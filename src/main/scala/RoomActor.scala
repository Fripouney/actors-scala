package fr.cytech.icc

import java.time.OffsetDateTime
import java.util.UUID
import scala.collection.immutable.SortedSet

import org.apache.pekko.actor.typed.{ ActorRef, Behavior }
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.Actor

enum Message {
  case CreatePost(author: String, content: String)
  case ListPosts(replyTo: ActorRef[SortedSet[Post]])
  case LatestPost(replyTo: ActorRef[Option[Post]])
  case GetPost(id: UUID, replyTo: ActorRef[Option[Post]])
}

case class Post(id: UUID, author: String, postedAt: OffsetDateTime, content: String)

given Ordering[Post] = Ordering.by(_.postedAt)

case class RoomActor(name: String) {

  private def handle(posts: SortedSet[Post]): Behavior[Message] = {
    Behaviors.receiveMessage {
      case Message.CreatePost(author, content) => 
        val id = UUID.randomUUID()
        val dateTime = OffsetDateTime.now()
        val newPost = new Post(id, author, dateTime, content)
        handle(posts + newPost)
        
      case Message.ListPosts(replyTo) =>
        replyTo.!(posts)
        Behaviors.same

      case Message.LatestPost(replyTo) =>
        replyTo.!(posts.lastOption)
        Behaviors.same

      case Message.GetPost(id, replyTo) =>
        replyTo.!(posts.find(p => p.id == id))
        Behaviors.same
    }
  }
}

object RoomActor {
  def apply(name: String): Behavior[Message] = new RoomActor(name).handle(SortedSet.empty)
}
