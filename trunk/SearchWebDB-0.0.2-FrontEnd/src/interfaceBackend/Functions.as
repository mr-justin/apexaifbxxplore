package interfaceBackend
{
	import flash.net.URLRequest;
	import mx.collections.ArrayCollection;
	
	/**
	 * This class contains functions that can be used in a static manner
	 * @author tpenin
	 */
	public class Functions
	{
		public function Functions() {}

		/**
		 * Returns a list of words given the string representing the user input.
		 */
		public static function getListFromString(s:String) : ArrayCollection {
			// ArrayCollection to contain the result
			var temp:ArrayCollection = new ArrayCollection;
			// Boolean set to true if a " is opened
			var opened:Boolean = false;
			// Temporary string
			var acc:String = "";
			// Browse the string
			for(var i:int = 0; i < s.length; i++) {
				// Get the character
				var str:String = s.charAt(i);
				// If it does not match an accepted character, we drop it
				var pattern:RegExp = /[A-Za-z0-9]/;
				if(str != " " && str != "\"" && !pattern.test(str))
					continue;
				// If it is an opening "
				if(str == "\"" && !opened) {
					opened = true;
					continue;
				}
				// If it is a closing "
				if(str == "\"" && opened) {
					opened = false;
					// Put the acc string into the list
					temp.addItem(acc);
					acc = "";
					continue;
				}
				// If it is a space not between "
				if(str == " " && !opened) {
					if(acc != "") {
						temp.addItem(acc);
						acc = "";
					}
					continue;
				}
				// If it is a space between "
				if(str == " " && opened) {
					acc += " ";
					continue;
				}
				// Else, add the char
				acc += str;
			}
			if(acc != "")
				temp.addItem(acc);		
			return temp;
		}
		
		/**
		 * Prepare a keyword string for display in the history bar
		 * @param s The string to consider
		 */
		public static function prepareStringForDisplay(s:String) : String {
			// Temporary string
			var acc:String = "";
			// Browse the string
			for(var i:int = 0; i < s.length; i++) {
				// Get the character
				var str:String = s.charAt(i);
				// If it does not match an accepted character, we drop it
				var pattern:RegExp = /[A-Za-z]/;
				if(str != " " && str != "\"" && !pattern.test(str))
					continue;
				// If it is a space
				if(str == " ") {
					if(acc.length > 0 && acc.charAt(acc.length - 1) != "") {
						acc += " ";
					}
					continue;
				}
				// Else, add the char
				acc += str;
			}	
			return acc;
		}
		
		/**
		 * Open the URL passed to this method in a new web page
		 */
	    public static function goToURL(urlStr:String) : void {
	        var webPageURL:URLRequest = new URLRequest(urlStr);
	        flash.net.navigateToURL(webPageURL, '_blank');  
	    }
	        
	    /**
	     * Open a new email form in user's email program with emailStr in the "to" address
	     */
	    public static function goToEmail(emailStr:String) : void {      
	        var emailURL:URLRequest = new URLRequest("mailto:" + emailStr);    
	        flash.net.navigateToURL(emailURL);
	    }
	}
}