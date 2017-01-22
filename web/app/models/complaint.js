var mongoose     = require('mongoose');
var Schema       = mongoose.Schema;

var ComplaintSchema   = new Schema({
    email: String,
    address: String,
    imagePath: String,
    date : Date,
    detail: String,
    status: String,
    starred: Boolean
});

module.exports = mongoose.model('Complaint', ComplaintSchema);