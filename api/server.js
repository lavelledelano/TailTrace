require('dotenv').config();
const mongoose = require('mongoose');
const app = require('./app');

const PORT = process.env.PORT || 3000;

mongoose.connect(process.env.MONGODB_URI)
  .then(() => app.listen(PORT, () => console.log('TailTrace API on port ' + PORT)))
  .catch((err) => { console.error('DB connection failed', err); process.exit(1); });