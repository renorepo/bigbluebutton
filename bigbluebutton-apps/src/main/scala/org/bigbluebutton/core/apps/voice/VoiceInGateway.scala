package org.bigbluebutton.core.apps.voice

import org.bigbluebutton.core.BigBlueButtonGateway
import org.bigbluebutton.core.api._

class VoiceInGateway(bbbGW: BigBlueButtonGateway) {
	
  def muteAllExceptPresenter(meetingID: String, requesterID: String, mute: Boolean) {
    bbbGW.accept(new MuteAllExceptPresenterRequest(meetingID, requesterID, mute))
  }
  
  def muteAllUsers(meetingID: String, requesterID: String, mute: Boolean) {
	  bbbGW.accept(new MuteMeetingRequest(meetingID, requesterID, mute))
	}
	
	def isMeetingMuted(meetingID: String, requesterID: String) {
	  bbbGW.accept(new IsMeetingMutedRequest(meetingID, requesterID))
	}
	
	def muteUser(meetingID: String, requesterID: String, userID: String, mute: Boolean) {
	  bbbGW.accept(new MuteUserRequest(meetingID, requesterID, userID, mute))
	}
	
	def lockUser(meetingID: String, requesterID: String, userID: String, lock: Boolean) {
	  bbbGW.accept(new LockUserRequest(meetingID, requesterID, userID, lock))
	}
	
	def ejectUserFromVoice(meetingID: String, userId: String, ejectedBy: String) {
	  bbbGW.accept(new EjectUserFromVoiceRequest(meetingID, userId, ejectedBy))
	}
	
	def voiceUserStatusChanged(voiceConf: String, voiceUserId: String, username: String, authCode: String, 
	                   muted: Boolean, talking: Boolean, userId: String, calledFromBbb: Boolean) {
	  bbbGW.accept(new VoiceUserStatusChangedMessage(voiceConf, voiceConf, voiceUserId, username, 
	                          username, authCode, muted, talking, userId, calledFromBbb))
	}
	
	def voiceConferenceRecordingStarted(voiceConf: String, filename: String, timestamp: String) {
	  bbbGW.accept(new VoiceConferenceRecordingStartedMessage(voiceConf, voiceConf, filename, timestamp))	  
	}
	
	def inviteUserIntoVoiceConference(meetingId: String, userNumber: String, 
	     callerName: String, dialNumber: String, requesterId: String) {
	  bbbGW.accept(new InviteUserIntoVoiceConfRequest(meetingId,
                       userNumber, callerName, dialNumber, requesterId))
	}
	
	def voiceConferenceRecordingStopped(voiceConf: String, timestamp: String) {
	  bbbGW.accept(new VoiceConferenceRecordingStoppedMessage(voiceConf, voiceConf, timestamp))	  
	}	
	
	def voiceUserLeftVoiceConf(voiceConf: String, userId: String) {
	  bbbGW.accept(new VoiceUserLeftConfMessage(voiceConf, voiceConf, userId))
	}
	
	def voiceUserJoined(meetingId: String, userId: String, webUserId: String, 
	                            conference: String, callerIdNum: String, 
	                            callerIdName: String,
								muted: Boolean, talking: Boolean) {
//	  println("VoiceInGateway: Got voiceUserJoined message for meeting [" + meetingId + "] user[" + callerIdName + "]")
	  val voiceUser = new VoiceUser(userId, webUserId, 
	                                callerIdName, callerIdNum,  
	                                true, false, muted, talking)
	  bbbGW.accept(new VoiceUserJoined(meetingId, voiceUser))
	}
	
	def voiceUserLeft(meetingId: String, userId: String) {
//	  println("VoiceInGateway: Got voiceUserLeft message for meeting [" + meetingId + "] user[" + userId + "]")
	  bbbGW.accept(new VoiceUserLeft(meetingId, userId))
	}
	
	def voiceUserLocked(meetingId: String, userId: String, locked: Boolean) {
	  bbbGW.accept(new VoiceUserLocked(meetingId, userId, locked))
	}
	
	def voiceUserMuted(meetingId: String, userId: String, muted: Boolean) {
	  bbbGW.accept(new VoiceUserMuted(meetingId, userId, muted))
	}
	
	def voiceUserTalking(meetingId: String, userId: String, talking: Boolean) {
	  bbbGW.accept(new VoiceUserTalking(meetingId, userId, talking))
	}
	
	def voiceRecording(meetingId: String, recordingFile: String, 
			            timestamp: String, recording: java.lang.Boolean) {
	  bbbGW.accept(new VoiceRecording(meetingId, recordingFile, 
			            timestamp, recording))
	}
}