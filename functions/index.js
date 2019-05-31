const functions = require('firebase-functions');

const request = require('request-promise')

//exports.indexPostsToElastic = functions.database.ref('/posts/{post_id}')
exports.indexPostsToElastic = functions.database.ref('/posts/{post_id}')
	.onWrite(event => {
		let postData = event.data.val();
		let post_id = event.params.post_id;
		
		console.log('Indexing post:', postData);
		
		let elasticSearchConfig = functions.config().elasticsearch;
        //let elasticSearchUrl = elasticSearchConfig.url + 'posts/post/' + post_id;
        let elasticSearchUrl = elasticSearchConfig.url + 'posts/' + post_id;
		let elasticSearchMethod = postData ? 'POST' : 'DELETE';
		
		let elasticSearchRequest = {
			method: elasticSearchMethod,
			url: elasticSearchUrl,
			auth:{
				username: elasticSearchConfig.username,
				password: elasticSearchConfig.password,
			},
			body: postData,
			json: true
          };
          
          //return request(elasticSearchRequest);
		  
		  return request(elasticSearchRequest).then(response => {
             console.log("ElasticSearch response", response);
             return null;
		  });
	});

// //this is what defines functions
// const functions = require('firebase-functions');
// //the request itself
// //this library will help us to make http requests
// const request = require('request-promise');

// //this will tell firebase to export the function to the server
// exports.indexPostsToElastic = functions.database.ref('/posts/{post_id}')
//     .onWrite(event => {
//         let postData = event.data.val();
//         //an event is a data snapshot
//         //and is going to retreive all the data from the object
//         let post_id = event.params.post_id;
//         //let user_id = event.params.user_id;

//         //this will appear in Firebase -> Functions -> Logs
//         console.log('Indexing the post', postData);

//         //now we are going to take some parameters specific to elasticsearch
//         //the config we set in the console
//         let elasticSearchConfig = functions.config().elasticsearch;
//         let elasticSearchUrl = elasticSearchConfig.url + 'posts/post/' + post_id;
//         //We should be able to post and delete with this function
//         let elasticSearchMethod = postData ? 'POST' : 'DELETE';
//             //other triggers are .onDelete, .onCreate, .onUpdate
//         //here we will create the request

//         let elasticSearchRequest = {
//             method: elasticSearchMethod,
//             url: elasticSearchUrl,
//             auth:{
//                 username: elasticSearchConfig.username,
//                 password: elasticSearchConfig.password,
//             },
//             body: postData,
//             json: true
//         };

//         return request(elasticSearchRequest).then(response => {
//             console.log("ElasticSearch response", response);
//         })
//     });


