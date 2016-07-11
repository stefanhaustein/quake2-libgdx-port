# quake2-libgdx-port

This is a fork of quake2-playn-port and still work in progress.

Goals:

 - Become familiar with libgdx
 - Fix web sound
 - Make sure the web version works with all modern browsers
 - Make the code run on Android

Status: 

 - 2016-07-12: Web and Desktop should basically work with sound now.
 - 2016-06-12: Web download, decompression and conversion works.
 - 2016-06-11: Desktop version basically works (incl. libGDX scene2d "installer" UI) 
 - 2016-06-10: Desktop file donwload, extraction and conversion seems to work.

TODO:

 - Offer to provide a new URL on load errors without a page reload.
 - Remove log spam
 - Mouse support
 - Fullscreen
 - Better default window size
 - Firefox errors
 - Androd unzip problems
 - Port improvements back to libGDX

Demo of the old playn version: http://quake2playn.appspot.com/ 

- If you visit the demo with `https:`, you'll need to change the download link in the input field from `http:` to `https:`, too, as pointed out in issue #1. Ill fix this when I get a chance.

Our original April 1st 2010 Video of the GWT port: https://www.youtube.com/watch?v=XhMN0wlITLk

