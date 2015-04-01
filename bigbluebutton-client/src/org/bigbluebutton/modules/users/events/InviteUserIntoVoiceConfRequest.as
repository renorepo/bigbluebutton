package org.bigbluebutton.modules.users.events
{
  import flash.events.Event;
  
  public class InviteUserIntoVoiceConfRequest extends Event
  {
    public static const INVITE_USER_INTO_VOICE_CONF:String = "invite user into voice conference";
    
    public var userNumber: String;
    
    public function InviteUserIntoVoiceConfRequest(userNumber: String)
    {
      super(INVITE_USER_INTO_VOICE_CONF, true, false);
      this.userNumber = userNumber;
    }
  }
}