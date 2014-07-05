package au.com.onegeek.respite

import reactivemongo.bson.{BSONString, BSONObjectID}

/**
 * Created by mfellows on 4/07/2014.
 */
object DefaultImplicits {
  implicit def BSONObjectIdToString(s: BSONObjectID): String = s.stringify
  implicit def StringToBSONObjectId(s: String): BSONObjectID = BSONObjectID(s)
//  implicit val s: String => BSONObjectID = s => BSONObjectID(s)
}