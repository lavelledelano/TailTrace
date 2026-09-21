const mongoose = require('mongoose');

const pointField = {
  type: { type: String, enum: ['Point'], default: 'Point' },
  coordinates: { type: [Number], required: true }, // [lng, lat]
};

const User = mongoose.model('User', new mongoose.Schema({
  fullName: { type: String, required: true, maxlength: 100 },
  email: { type: String, required: true, unique: true },
  phone: String,
  passwordHash: { type: String, required: true },
}, { timestamps: true }));

const petSchema = new mongoose.Schema({
  owner: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  petName: { type: String, required: true },
  species: { type: String, required: true },
  breed: { type: String, required: true },
  color: { type: String, required: true },
  description: String,
  photo: Buffer,
  status: { type: String, enum: ['ACTIVE', 'PAUSED', 'REUNITED', 'CLOSED'], default: 'ACTIVE' },
  location: pointField,
  lastSeenAt: { type: Date, default: Date.now },
}, { timestamps: true });
petSchema.index({ location: '2dsphere' });
const Pet = mongoose.model('Pet', petSchema);

const Sighting = mongoose.model('Sighting', new mongoose.Schema({
  pet: { type: mongoose.Schema.Types.ObjectId, ref: 'Pet', required: true, index: true },
  spotter: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  direction: String,
  notes: String,
  photo: Buffer,
  hasPhoto: { type: Boolean, default: false },
  location: pointField,
  seenAt: { type: Date, default: Date.now },
}));

module.exports = { User, Pet, Sighting };