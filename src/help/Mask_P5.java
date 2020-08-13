package help;

import java.util.ArrayList;
import com.jogamp.opengl.*;
import processing.core.PApplet;
import processing.opengl.PGL;

public class Mask_P5 extends PApplet {

public static void main(String[] args) {
    PApplet.main(Mask_P5.class, args);
}


PGL pgl;
GL2 gl;


Rect current_rect;

public void settings() {
    size(600, 600, P3D);
}


public void setup() {

    println(PGL.GEQUAL);
    
    noStroke();
    
    // will have a depth of 0, equal to the stencil buffer after a clear
    current_rect = new Rect(null, 0, 0, 640, 480);
}

static class Rect {
    float x, y, w, h;
    ArrayList<Rect> children = new ArrayList<Rect>();
    Rect parent;
    int mask_depth;
    Rect(Rect parent, float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        if (parent != null) {
            this.parent = parent;
            this.mask_depth = parent.mask_depth + 1;
            parent.children.add(this);
        }
    }
}    



public void draw() {
    
    
    pgl = beginPGL();

    // gl = ((PJOGL)pgl).gl.getGL2(); // Not a GL2 implementation
    // gl = ((PJOGL)pgl).gl.getGL3bc(); // Not a GL3bc implementation
    // gl = ((PJOGL)pgl).gl.getGL4bc(); // Not a GL4bc implementation
    // etc. I tried all, either an error, or a grey screen and opengl silently failing


    pgl.enable(PGL.STENCIL_TEST);
    pgl.clearStencil(0x0);
    pgl.clear(PGL.STENCIL_BUFFER_BIT);

    background(0);
    
    begin_mask(100, 100, 400, 400); // A
    fill(255,0,0);
    rect(100, 100, 400, 400);

        begin_mask(50, 150, 500, 100); // B
        fill(0,255,0);
        rect(50, 150, 500, 100);

            begin_mask(200, 50, 100, 500); // C
            fill(0,0,255);
            rect(200, 50, 100, 500);
            end_mask(); // C

        end_mask(); // B

    end_mask(); // A
    
    endPGL();
}



void begin_mask(float x, float y, float w, float h) {
    current_rect = new Rect(current_rect, x, y, w, h);
    pgl.colorMask(false, false, false, false);
    pgl.depthMask(false);
    // we want to write to the stencil buffer
    pgl.stencilMask(0xff);
    // we pass if the value in the stencil buffer is equal to the depth of the
    // parent of the current_rect
    pgl.stencilFunc(PGL.EQUAL, current_rect.parent.mask_depth, 0xFF);
    int action_if_stencil_test_fails = PGL.KEEP;
    int action_if_stencil_test_passes_but_depth_pass_fails = PGL.KEEP;
    // we increment, so the stencil value in the buffer get's equal
    // to the mask_depth of the current_rect
    int action_if_both_the_stencil_and_depth_pass_succeed = PGL.INCR;

    pgl.stencilOp(
        action_if_stencil_test_fails,
        action_if_stencil_test_passes_but_depth_pass_fails,
        action_if_both_the_stencil_and_depth_pass_succeed
    );

    // now it should increment the value by one for only the part that is inside the part
    // of current_rect.parent
    // gl_rect(x, y, w, h);
    rect(x, y, w, h);
    enable_normal_draw_mode();
}


void end_mask() {
    // decrement stencil mask as if we never existed
    pgl.stencilMask(0xff);
    pgl.stencilFunc(PGL.EQUAL, current_rect.mask_depth, 0xff);
    pgl.stencilOp(PGL.KEEP, PGL.KEEP, PGL.DECR);
    pgl.colorMask(false, false, false, false);
    pgl.depthMask(false);
    // gl_rect(current_rect.x, current_rect.y, current_rect.w, current_rect.h);
    rect(current_rect.x, current_rect.y, current_rect.w, current_rect.h);
    current_rect = current_rect.parent;
    enable_normal_draw_mode();
}


void enable_normal_draw_mode() {
    pgl.stencilMask(0x00);

    // for normal drawing, we only wan't to be able to draw where
    // the stencil value in the buffer is equal to the mask_depth
    // of the current_rect
    pgl.stencilFunc(PGL.EQUAL, current_rect.mask_depth, 0xff);
    pgl.stencilOp(PGL.KEEP, PGL.KEEP, PGL.KEEP);
    pgl.colorMask(true, true, true, true);
    pgl.depthMask(true);
}



void gl_rect(float x1, float y1, float w, float h) {

    float x2 = x1 + w;
    float y2 = y1 + h;

    gl.glBegin(GL2.GL_QUADS);
    gl.glVertex2f(x1, y1);
    gl.glVertex2f(x2, y1);
    gl.glVertex2f(x2, y2);
    gl.glVertex2f(x1, y2);
    gl.glEnd();
}

}