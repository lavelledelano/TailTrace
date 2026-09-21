const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { User, Pet, Sighting } = require('./models');

const app = express();
app.use(cors());
app.use(express.json({ limit: '6mb' })); // photos arrive as base64

const SECRET = process.env.JWT_SECRET || 'dev-secret-change-me';
const wrap = (fn) => (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);

const sign = (u) => jwt.sign({ id: u._id }, SECRET, { expiresIn: '7d' });
const userDTO = (u) => ({ id: u._id, fullName: u.fullName, email: u.email, phone: u.phone || null });
const petDTO = (p) => ({
  id: p._id,
  petName: p.petName, species: p.species, breed: p.breed, color: p.color,
  description: p.description || '', status: p.status,
  lat: p.location.coordinates[1], lng: p.location.coordinates[0],
  lastSeenAt: p.lastSeenAt,
  photoUrl: '/api/pets/' + p._id + '/photo',
  ownerId: String(p.owner._id || p.owner),
  ownerName: p.owner.fullName, ownerPhone: p.owner.phone,
});
const sightingDTO = (s) => ({
  id: s._id, direction: s.direction || '', notes: s.notes || '',
  lat: s.location.coordinates[1], lng: s.location.coordinates[0],
  seenAt: s.seenAt,
  photoUrl: s.hasPhoto ? '/api/sightings/' + s._id + '/photo' : null,
});

function auth(req, res, next) {
  const h = req.headers.authorization || '';
  const token = h.startsWith('Bearer ') ? h.slice(7) : null;
  if (!token) return res.status(401).json({ error: 'Please sign in' });
  try {
    req.userId = jwt.verify(token, SECRET).id;
    next();
  } catch {
    res.status(401).json({ error: 'Session expired, please sign in again' });
  }
}

app.get('/', (req, res) => res.json({ status: 'TailTrace API running' }));

// ---------- AUTH ----------
app.post('/api/auth/register', wrap(async (req, res) => {
  const { fullName, email, password, phone } = req.body;
  if (!fullName || !email || !password || password.length < 8)
    return res.status(400).json({ error: 'Name, email and a password of 8+ characters are required' });
  const exists = await User.findOne({ email: email.toLowerCase() });
  if (exists) return res.status(409).json({ error: 'That email is already registered' });
  const passwordHash = await bcrypt.hash(password, 10);
  const user = await User.create({ fullName, email: email.toLowerCase(), phone, passwordHash });
  res.status(201).json({ token: sign(user), user: userDTO(user) });
}));

app.post('/api/auth/login', wrap(async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ error: 'Email and password are required' });
  const user = await User.findOne({ email: email.toLowerCase() });
  if (!user || !(await bcrypt.compare(password, user.passwordHash)))
    return res.status(401).json({ error: 'Incorrect email or password' });
  res.json({ token: sign(user), user: userDTO(user) });
}));

app.put('/api/users/me', auth, wrap(async (req, res) => {
  const { fullName, phone } = req.body;
  if (!fullName) return res.status(400).json({ error: 'Name is required' });
  const user = await User.findByIdAndUpdate(req.userId, { fullName, phone }, { new: true });
  res.json(userDTO(user));
}));

// ---------- PETS ----------
app.post('/api/pets', auth, wrap(async (req, res) => {
  const { petName, species, breed, color, description, photoBase64, lat, lng, lastSeenAt } = req.body;
  if (!petName || !species || !breed || !color || !photoBase64 || lat == null || lng == null)
    return res.status(400).json({ error: 'Pet name, species, breed, colour, photo and location are required' });
  const pet = await Pet.create({
    owner: req.userId, petName, species, breed, color, description,
    photo: Buffer.from(photoBase64, 'base64'),
    location: { type: 'Point', coordinates: [lng, lat] },
    lastSeenAt: lastSeenAt || new Date(),
  });
  res.status(201).json(petDTO(pet));
}));

// must be defined BEFORE /api/pets/:id
app.get('/api/pets/nearby', auth, wrap(async (req, res) => {
  const lat = parseFloat(req.query.lat);
  const lng = parseFloat(req.query.lng);
  const radiusKm = parseFloat(req.query.radiusKm) || 10;
  if (Number.isNaN(lat) || Number.isNaN(lng))
    return res.status(400).json({ error: 'lat and lng are required' });
  const pets = await Pet.find({
    status: 'ACTIVE',
    location: { $near: { $geometry: { type: 'Point', coordinates: [lng, lat] }, $maxDistance: radiusKm * 1000 } },
  }).select('-photo').limit(50);
  res.json(pets.map(petDTO));
}));

app.get('/api/pets/:id', auth, wrap(async (req, res) => {
  const pet = await Pet.findById(req.params.id).select('-photo').populate('owner', 'fullName phone');
  if (!pet) return res.status(404).json({ error: 'Case not found' });
  res.json(petDTO(pet));
}));

app.get('/api/pets/:id/photo', wrap(async (req, res) => {
  const pet = await Pet.findById(req.params.id).select('photo');
  if (!pet || !pet.photo) return res.sendStatus(404);
  res.type('image/jpeg').send(pet.photo);
}));

app.patch('/api/pets/:id/status', auth, wrap(async (req, res) => {
  const { status } = req.body;
  if (!['ACTIVE', 'PAUSED', 'REUNITED', 'CLOSED'].includes(status))
    return res.status(400).json({ error: 'Invalid status' });
  const pet = await Pet.findById(req.params.id).select('-photo');
  if (!pet) return res.status(404).json({ error: 'Case not found' });
  if (String(pet.owner) !== req.userId) return res.status(403).json({ error: 'Only the owner can change this case' });
  pet.status = status;
  await pet.save();
  await pet.populate('owner', 'fullName phone');
  res.json(petDTO(pet));
}));

// ---------- SIGHTINGS ----------
app.post('/api/sightings', auth, wrap(async (req, res) => {
  const { petId, lat, lng, direction, notes, photoBase64 } = req.body;
  if (!petId || lat == null || lng == null)
    return res.status(400).json({ error: 'petId and location are required' });
  const s = await Sighting.create({
    pet: petId, spotter: req.userId, direction, notes,
    photo: photoBase64 ? Buffer.from(photoBase64, 'base64') : undefined,
    hasPhoto: !!photoBase64,
    location: { type: 'Point', coordinates: [lng, lat] },
  });
  res.status(201).json(sightingDTO(s));
}));

app.get('/api/pets/:id/sightings', auth, wrap(async (req, res) => {
  const list = await Sighting.find({ pet: req.params.id }).select('-photo').sort({ seenAt: -1 }).limit(50);
  res.json(list.map(sightingDTO));
}));

app.get('/api/sightings/:id/photo', wrap(async (req, res) => {
  const s = await Sighting.findById(req.params.id).select('photo');
  if (!s || !s.photo) return res.sendStatus(404);
  res.type('image/jpeg').send(s.photo);
}));

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'Server error' });
});

module.exports = app;