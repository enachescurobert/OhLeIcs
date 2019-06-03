PUT 

http://35.228.155.106//elasticsearch/posts?include_type_name=true

{
	"mappings":{
	"_doc":{
		"properties":{
				"city":{
					"type": "text"
				},
				"contact_email":{
					"type": "text"
				},
				"country":{
					"type": "text"
				},
				"description":{
				"type": "text"
				},
				"image":{
				"type": "text"
				},
				"post_id":{
					"type": "text"
				},
				"state_province":{
					"type": "text"
				},
				"title":{
					"type": "text"
				},
				"user_id":{
					"type": "text"
				}
			}
		}
	}
}

Content-Type application/json
Authorization Basic dXNlcjpibnRxOTFtS0xDbzc=

