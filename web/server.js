
var express    = require('express');        
var app        = express();                 
var bodyParser = require('body-parser');

var mongoose   = require('mongoose');
mongoose.connect('mongodb://localhost:27017/cleancity');

var Complaint     = require('./app/models/complaint');

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port = process.env.PORT || 8080;       

var router = express.Router();            

router.use(function(req, res, next) {

    console.log('Something is happening.');
    next();
});

router.get('/', function(req, res) {
    res.json({ message: 'hooray! welcome to our api!' });   
});

router.route('/complaints')

    .post(function(req, res) {

    	var fs = require('fs');
		var data = req.body.image;
		var buf = new Buffer(data, 'base64');
        
        var complaint = new Complaint();     
        complaint.email = req.body.email;  
        complaint.address = req.body.address;
        complaint.imagePath = "http://192.168.1.133:8080/images/";
        complaint.date = req.body.date;
        complaint.detail = req.body.detail;
        complaint.status = "Pending";
        complaint.starred = false;

        complaint.save(function(err, com) {
            if (err)
                res.send(err);
            fs.writeFile('./images/' + com.id + '.png', buf);
            res.json({ message: 'Complaint created!' });
        });
        
    });

router.route('/complaints/:user_id')

    .get(function(req, res) {
        Complaint.find({ email: req.params.user_id }).sort({_id: -1}).limit(10).exec(function(err, complaints) {
            if (err)
                res.send(err);
            res.json(complaints);
        });
    });

app.use('/images', express.static(__dirname + '/images'));

app.use('/api', router);

app.listen(port);
console.log('Magic happens on port ' + port);