social.users
{
  _id: String (userid),
  schemaversion: (integer),
  nicknames: String,
  dateCreated: Date(),
  follows: ArrayOfString (people followed) [Indexed]
}


social.posts
{
 _id: ObjectId(),
 schemaVersion: Int,
 postedBy: String,
 date: Date,
 text: String
}

use social
db.users.createIndex({nicknames:1},{unique:true,sparse:true})

// We need an index on originalName anyway - we can combine that with follows and make it unique
// however we need to make it only apply to records where originalName exists
// We also need an index  on followsize otherwise we will check each bucket before creating a new one.
db.users.createIndex({originalName:1, followSize:1})
db.users.createIndex({originalName:1, follows:1}, { unique: true, partialFilterExpression: { originalName: { "$exists": true } }})

db.posts.createIndex({status: 1}, {diststart: 1})