package org.bigbluebutton.core

import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.HashMap
import org.bigbluebutton.core.api._
import org.bigbluebutton.core.util._
import org.bigbluebutton.core.api.ValidateAuthTokenTimedOut

class BigBlueButtonActor(outGW: MessageOutGateway) extends Actor with LogHelper {

  private var meetings = new HashMap[String, MeetingActor]
  
 
  def act() = {
	loop {
		react {
	      case msg: CreateMeeting                 => handleCreateMeeting(msg)
	      case msg: DestroyMeeting                => handleDestroyMeeting(msg)
	      case msg: KeepAliveMessage              => handleKeepAliveMessage(msg)
	      case msg: ValidateAuthToken             => handleValidateAuthToken(msg)
          case msg: GetAllMeetingsRequest         => handleGetAllMeetingsRequest(msg)
	      case msg: InMessage                     => handleMeetingMessage(msg)
	      case _ => // do nothing
	    }
	  }
  }
  
  private def handleValidateAuthToken(msg: ValidateAuthToken) {
    meetings.get(msg.meetingID) foreach { m =>
      m !? (3000, msg) match {
        case None => {
          logger.warn("Failed to get response to from meeting=" + msg.meetingID + "]. Meeting has probably hung.")
          outGW.send(new ValidateAuthTokenTimedOut(msg.meetingID, msg.userId, msg.token, false, msg.correlationId, msg.sessionId))
        }
        case Some(rep) => {
        /**
         * Received a reply from MeetingActor which means hasn't hung!
         * Sometimes, the actor seems to hang and doesn't anymore accept messages. This is a simple
         * audit to check whether the actor is still alive. (ralam feb 25, 2015)
         */
        }
      }   
    }      
  }
  
  private def handleMeetingMessage(msg: InMessage):Unit = {
    msg match {
      case ucm: UserConnectedToGlobalAudio => {
        val m = meetings.values.find( m => m.voiceBridge == ucm.voiceConf)
        m foreach {mActor => mActor ! ucm}
      }
      case udm: UserDisconnectedFromGlobalAudio => {
        val m = meetings.values.find( m => m.voiceBridge == udm.voiceConf)
        m foreach {mActor => mActor ! udm}        
      }
      case udm: VoiceConferenceRecordingStartedMessage => {
        //println("Handling VoiceUserStatusChangedMessage message")
        val m = meetings.values.find( m => m.voiceBridge == udm.voiceConf)
        m foreach {mActor => mActor ! udm}        
      }
      case udm: VoiceConferenceRecordingStoppedMessage => {
        //println("Handling VoiceUserStatusChangedMessage message")
        val m = meetings.values.find( m => m.voiceBridge == udm.voiceConf)
        m foreach {mActor => mActor ! udm}        
      }
      case udm: VoiceUserStatusChangedMessage => {
        //println("Handling VoiceUserStatusChangedMessage message")
        val m = meetings.values.find( m => m.voiceBridge == udm.voiceConf)
        m foreach {mActor => mActor ! udm}        
      }
      case udm: VoiceUserLeftConfMessage => {
        val m = meetings.values.find( m => m.voiceBridge == udm.voiceConf)
        m foreach {mActor => mActor ! udm}        
      }
      case allOthers => {
		    meetings.get(allOthers.meetingID) match {
		      case None => handleMeetingNotFound(allOthers)
		      case Some(m) => {
		       // log.debug("Forwarding message [{}] to meeting [{}]", msg.meetingID)
		        m ! allOthers
		      }
		    }        
      }
    }
  }
  
  private def handleMeetingNotFound(msg: InMessage) {
    msg match {
      case vat:ValidateAuthToken => {
        logger.info("No meeting [" + vat.meetingID + "] for auth token [" + vat.token + "]")
        outGW.send(new ValidateAuthTokenReply(vat.meetingID, vat.userId, vat.token, false, vat.correlationId))
      }
      case _ => {
        logger.info("No meeting [" + msg.meetingID + "] for message type [" + msg.getClass() + "]")
        // do nothing
      }
    }
  }

  private def handleKeepAliveMessage(msg: KeepAliveMessage):Unit = {
    outGW.send(new KeepAliveMessageReply(msg.aliveID))
  }
    
  private def handleDestroyMeeting(msg: DestroyMeeting) {
    logger.info("BBBActor received DestroyMeeting message for meeting id [" + msg.meetingID + "]")
    meetings.get(msg.meetingID) match {
      case None => logger.info("Could not find meeting id[" + msg.meetingID + "] to destroy.")
      case Some(m) => {
        m ! StopMeetingActor
        meetings -= msg.meetingID    

        logger.info("Kicking everyone out of meeting id[" + msg.meetingID + "].")
        outGW.send(new EndAndKickAll(msg.meetingID, m.recorded))
        
        logger.info("Destroyed meeting id[" + msg.meetingID + "].")
        outGW.send(new MeetingDestroyed(msg.meetingID))
      }
    }
  }
  
  private def handleCreateMeeting(msg: CreateMeeting):Unit = {
    meetings.get(msg.meetingID) match {
      case None => {
        logger.info("New meeting create request [" + msg.meetingName + "]")
    	  var m = new MeetingActor(msg.meetingID, msg.externalMeetingID, msg.meetingName, msg.recorded, 
    	                  msg.voiceBridge, msg.duration, 
    	                  msg.autoStartRecording, msg.allowStartStopRecording, msg.moderatorPass,
    	                  msg.viewerPass, msg.createTime, msg.createDate,
    	                  outGW)
    	  m.start
    	  meetings += m.meetingID -> m
    	  outGW.send(new MeetingCreated(m.meetingID, m.externalMeetingID, m.recorded, m.meetingName, m.voiceBridge, msg.duration,
    	                     msg.moderatorPass, msg.viewerPass, msg.createTime, msg.createDate))
    	  
    	  m ! new InitializeMeeting(m.meetingID, m.recorded)
    	  m ! "StartTimer"
      }
      case Some(m) => {
        logger.info("Meeting already created [" + msg.meetingName + "]")
        // do nothing
      }
    }
  }

  private def handleGetAllMeetingsRequest(msg: GetAllMeetingsRequest) {
    var len = meetings.keys.size
    println("meetings.size=" + meetings.size)
    println("len_=" + len)

    val set = meetings.keySet
    val arr : Array[String] = new Array[String](len)
    set.copyToArray(arr)
    val resultArray : Array[MeetingInfo] = new Array[MeetingInfo](len)

    for(i <- 0 until arr.length) {
      val id = arr(i)
      val duration = meetings.get(arr(i)).head.getDuration()
      val name = meetings.get(arr(i)).head.getMeetingName()
      val recorded = meetings.get(arr(i)).head.getRecordedStatus()
      val voiceBridge = meetings.get(arr(i)).head.getVoiceBridgeNumber()

      var info = new MeetingInfo(id, name, recorded, voiceBridge, duration)
      resultArray(i) = info

      //remove later
      println("for a meeting:" + id)
      println("Meeting Name = " + meetings.get(id).head.getMeetingName())
      println("isRecorded = " + meetings.get(id).head.getRecordedStatus())
      println("voiceBridge = " + voiceBridge)
      println("duration = " + duration)

      //send the users
      this ! (new GetUsers(id, "nodeJSapp"))

      //send the presentation
      this ! (new GetPresentationInfo(id, "nodeJSapp", "nodeJSapp"))

      //send chat history
      this ! (new GetChatHistoryRequest(id, "nodeJSapp", "nodeJSapp"))
    }

    outGW.send(new GetAllMeetingsReply(resultArray))
  }

}
