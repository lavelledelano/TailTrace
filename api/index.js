const mongoose = require('mongoose');
const app = require('./app');

async function connect() {
  if (mongoose.connection.readyState === 1) return;
  await mongoose.connect(process.env.MONGODB_URI);
}

module.exports = async (req, res) => {
  try {
    await connect();
  } catch (err) {
    console.error('DB connection failed', err);
    return res.status(500).json({ error: 'Database connection failed' });
  }
  return app(req, res);
};