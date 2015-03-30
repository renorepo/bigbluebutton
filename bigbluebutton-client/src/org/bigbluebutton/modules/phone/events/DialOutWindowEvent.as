package org.bigbluebutton.modules.phone.events
{
	import flash.events.Event;
	
	public class DialOutWindowEvent extends Event
	{
		public static const SHOW_DIAL_OUT_WINDOW:String = 'phone show dial out window event';
		public static const DIAL_OUT_WINDOW_CLOSED:String = 'phone dial out window closed event';
		
		public function DialOutWindowEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}