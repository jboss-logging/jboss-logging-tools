function annotationLinks() {
    var links = document.getElementsByTagName("a");
    for(var i = 0, max = links.length; i < max; i++) {
        var l = links[i];
        if (l.text.startsWith("@")) {
            l.href = l.href + "/org/jboss/logging/annotations/" + l.text.substring(1) + ".html";
        }
    }
}