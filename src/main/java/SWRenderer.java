import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SWRenderer extends JFrame {
    public static void main(String[] args) {
        new SWRenderer();
    }
	/*
		Projection Size = C * 1/Distance to Camera
	*/

    private static int width = 500;
    private static int height = 500;

    Vec3[] vertices = new Vec3[]{
            new Vec3(-1, -1, -1),
            new Vec3(+1, -1, -1),
            new Vec3(+1, +1, -1),
            new Vec3(-1, +1, -1),

            new Vec3(-1, -1, +1),
            new Vec3(+1, -1, +1),
            new Vec3(+1, +1, +1),
            new Vec3(-1, +1, +1)
    };

    Vec3[] indexes = new Vec3[]{
            new Vec3(0, 1, 2),
            new Vec3(0, 2, 3),
            new Vec3(7, 6, 5),
            new Vec3(7, 5, 4),
            new Vec3(0, 3, 7),
            new Vec3(0, 7, 4),
            new Vec3(2, 1, 5),
            new Vec3(2, 5, 6),
            new Vec3(3, 2, 6),
            new Vec3(3, 6, 7),
            new Vec3(1, 0, 4),
            new Vec3(1, 4, 5)
    };

    Mat4 p = new Mat4(
            (float) width, 0f, width / 2f, 0f,
            0f, (float) width, height / 2f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f).transpose();

    Mat4 v = Mat4.translate(new Vec3(0, 0, 5));
    Mat4 m = Mat4.ID;

    SWRenderer() {
        super();
        setSize(width, height);
        setResizable(true);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().add(new JPanel() {
            @Override
            public void paint(Graphics _g) {
                super.paint(_g);

                var vertices2D = new Vec2[vertices.length];
                var angle = ((System.currentTimeMillis()/100 % 360));
                var r = Mat4.rotate((float) angle, new Vec3(1, 1, 1));

                var mvp = p.postMultiply(v).postMultiply(m).postMultiply(r);


                for (int i = 0, verticesCopyLength = vertices.length; i < verticesCopyLength; i++) {

                    var vertex3 = vertices[i];
                    var vertexTransformed = mvp.transform(new Vec4(vertex3.x, vertex3.y, vertex3.z, 1));
                    //normalize by homogeneous component
                    vertexTransformed = vertexTransformed.scale(1 / vertexTransformed.w);
                    vertices2D[i] = new Vec2(vertexTransformed.x, vertexTransformed.y);

                }

                int[] xPoints = new int[3];
                int[] yPoints = new int[3];

                for (Vec3 index : indexes) {
                    var a = vertices2D[(int) index.x];
                    var b = vertices2D[(int) index.y];
                    var c = vertices2D[(int) index.z];

                    xPoints[0] = (int) a.x;
                    yPoints[0] = (int) a.y;

                    xPoints[1] = (int) b.x;
                    yPoints[1] = (int) b.y;

                    xPoints[2] = (int) c.x;
                    yPoints[2] = (int) c.y;

                    _g.setColor(Color.BLACK);
                    _g.drawPolygon(xPoints, yPoints, 3);

                    xPoints = new int[3];
                    yPoints = new int[3];
                }
                repaint();
            }
        });
    }


}
