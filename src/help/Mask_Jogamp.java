package help;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class Mask_Jogamp {


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


Rect current_rect;

GL2 gl;

public static void main(String[] args) {
    Mask_Jogamp app = new Mask_Jogamp();
    app.start();
}


void start() {

    // will have a depth of 0, equal to the stencil buffer after a clear
    current_rect = new Rect(null, 0, 0, 640, 480);

    GLProfile glprofile = GLProfile.getDefault();
    GLCapabilities glcapabilities = new GLCapabilities(glprofile);
    glcapabilities.setStencilBits(8);

    final GLCanvas glcanvas = new GLCanvas(glcapabilities);

    glcanvas.addGLEventListener( new GLEventListener() {
        
        @Override
        public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {

            gl = glautodrawable.getGL().getGL2();

            gl.glMatrixMode( GL2.GL_PROJECTION );
            gl.glLoadIdentity();
    
            // coordinate system origin at lower left with width and height same as the window
            GLU glu = new GLU();
            glu.gluOrtho2D( 0.0f, width, 0.0f, height );
    
            gl.glMatrixMode( GL2.GL_MODELVIEW );
            gl.glLoadIdentity();
    
            gl.glViewport( 0, 0, width, height );
        }
        
        @Override
        public void init( GLAutoDrawable glautodrawable ) {
        }
        
        @Override
        public void dispose( GLAutoDrawable glautodrawable ) {
        }
        
        @Override
        public void display( GLAutoDrawable glautodrawable ) {

            int width = glautodrawable.getSurfaceWidth();
            int height = glautodrawable.getSurfaceHeight();
            
            gl.glEnable(GL.GL_STENCIL_TEST);
            gl.glClearStencil(0x0);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

            gl.glLoadIdentity();


            begin_mask(100, 100, 400, 400); // A
            gl.glColor3f(1, 0, 0);
            rect(100, 100, 400, 400);

                begin_mask(50, 150, 500, 100); // B
                gl.glColor3f(0, 1, 0);
                rect(50, 150, 500, 100);

                    begin_mask(200, 50, 100, 500); // C
                    gl.glColor3f(0, 0, 1);
                    rect(200, 50, 100, 500);
                    end_mask(); // C

                end_mask(); // B

            end_mask(); // A

        }
    });

    final Frame frame = new Frame( "One Triangle AWT" );
    frame.add( glcanvas );
    frame.addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent windowevent ) {
            frame.remove( glcanvas );
            frame.dispose();
            System.exit( 0 );
        }
    });

    frame.setSize( 640, 480 );
    frame.setVisible( true );
}



void rect(float x1, float y1, float w, float h) {

    float x2 = x1 + w;
    float y2 = y1 + h;

    gl.glBegin(GL2.GL_QUADS);
    gl.glVertex2f(x1, y1);
    gl.glVertex2f(x2, y1);
    gl.glVertex2f(x2, y2);
    gl.glVertex2f(x1, y2);
    gl.glEnd();
}


void begin_mask(float x, float y, float w, float h) {
    current_rect = new Rect(current_rect, x, y, w, h);
    gl.glColorMask(false, false, false, false);
    gl.glDepthMask(false);
    // we want to write to the stencil buffer
    gl.glStencilMask(0xff);
    // we pass if the value in the stencil buffer is equal to the depth of the
    // parent of the current_rect
    gl.glStencilFunc(GL2.GL_EQUAL, current_rect.parent.mask_depth, 0xFF);
    int action_if_stencil_test_fails = GL2.GL_KEEP;
    int action_if_stencil_test_passes_but_depth_pass_fails = GL2.GL_KEEP;
    // we increment, so the stencil value in the buffer get's equal
    // to the mask_depth of the current_rect
    int action_if_both_the_stencil_and_depth_pass_succeed = GL2.GL_INCR;

    gl.glStencilOp(
        action_if_stencil_test_fails,
        action_if_stencil_test_passes_but_depth_pass_fails,
        action_if_both_the_stencil_and_depth_pass_succeed
    );

    // now it should increment the value by one for only the part that is inside the part
    // of current_rect.parent
    rect(x, y, w, h);
    enable_normal_draw_mode();
}


void end_mask() {
    // decrement stencil mask as if we never existed
    gl.glStencilMask(0xff);
    gl.glStencilFunc(GL2.GL_EQUAL, current_rect.mask_depth, 0xff);
    gl.glStencilOp(GL2.GL_KEEP, GL2.GL_KEEP, GL2.GL_DECR);
    gl.glColorMask(false, false, false, false);
    gl.glDepthMask(false);
    rect(current_rect.x, current_rect.y, current_rect.w, current_rect.h);
    current_rect = current_rect.parent;
    enable_normal_draw_mode();
}


void enable_normal_draw_mode() {
    gl.glStencilMask(0x00);

    // for normal drawing, we only wan't to be able to draw where
    // the stencil value in the buffer is equal to the mask_depth
    // of the current_rect
    gl.glStencilFunc(GL2.GL_EQUAL, current_rect.mask_depth, 0xff);
    gl.glStencilOp(GL2.GL_KEEP, GL2.GL_KEEP, GL2.GL_KEEP);
    gl.glColorMask(true, true, true, true);
    gl.glDepthMask(true);
}


}