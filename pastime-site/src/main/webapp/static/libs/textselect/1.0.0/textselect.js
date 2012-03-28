define(["jquery"], function($) {

  $.fn.textselect = function() {
    var doc = document;
    var text = this[0];    
    if (doc.body.createTextRange) {
        var range = document.body.createTextRange();
        range.moveToElementText(text);
        range.select();
    } else if (window.getSelection) {
        var selection = window.getSelection();        
        var range = document.createRange();
        range.selectNodeContents(text);
        selection.removeAllRanges();
        selection.addRange(range);
    }    
    return this;
  };
  
});