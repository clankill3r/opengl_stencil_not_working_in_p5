# opengl stencil not working in p5

I'm working on this [gui library](https://github.com/clankill3r/writings/tree/master/EXTANT_GUI_Library) and for over a year now I'm partly stuck on using stencils inside processing.

In order to provide certain functionality in my gui library I need stencils to work.

This is what I get with jogl (which is correct):

![jogl](Screen%20Shot%202020-08-13%20at%2015.58.51.png)

This is what I get in processing (which is incorrect):

![processing](Screen%20Shot%202020-08-13%20at%2015.59.00.png)

That it is bigger and upside down does not matter, the focus is stencils here.

I included a jar of processing that includes the sources in this repository (so you can step threw using a debugger). Keep in mind that this is from processing 4.0a1 which needs the [openjdk 11](https://adoptopenjdk.net/upstream.html?variant=openjdk11&ga=ga).



**SOMEONE PLEASE HELP!**