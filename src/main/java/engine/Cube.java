package engine;

import util.Int3;
import util.Vec3;

public class Cube extends Mesh {
    public Cube(ModelMatrixFunction modelMatrixFunction, Rasterizer rasterizer) {
        super(modelMatrixFunction, rasterizer);

        vertices = new Vertex[]{
                new Vertex(new Vec3(-1, -1, -1), new Vec3(255, 0, 0), new Vec3(0, 0, 1)), // 0    //0
                new Vertex(new Vec3(-1, -1, -1), new Vec3(255, 0, 0), new Vec3(0, 1, 1)), // 0    //1
                new Vertex(new Vec3(-1, -1, -1), new Vec3(255, 0, 0), new Vec3(1, 1, 1)), // 0    //2

                new Vertex(new Vec3(+1, -1, -1), new Vec3(0, 255, 0), new Vec3(0, 1, 1)), // 1    //3
                new Vertex(new Vec3(+1, -1, -1), new Vec3(0, 255, 0), new Vec3(1, 1, 1)), // 1    //4
                new Vertex(new Vec3(+1, -1, -1), new Vec3(0, 255, 0), new Vec3(0, 1, 1)), // 1    //5

                new Vertex(new Vec3(+1, +1, -1), new Vec3(0, 0, 255), new Vec3(1, 1, 1)), // 2    //6
                new Vertex(new Vec3(+1, +1, -1), new Vec3(0, 0, 255), new Vec3(0, 1, 1)), // 2    //7
                new Vertex(new Vec3(+1, +1, -1), new Vec3(0, 0, 255), new Vec3(1, 1, 1)), // 2    //8

                new Vertex(new Vec3(-1, +1, -1), new Vec3(255, 0, 0), new Vec3(1, 0, 1)), // 3    //9
                new Vertex(new Vec3(-1, +1, -1), new Vec3(255, 0, 0), new Vec3(1, 1, 1)), // 3    //10
                new Vertex(new Vec3(-1, +1, -1), new Vec3(255, 0, 0), new Vec3(0, 1, 1)), // 3    //11

                new Vertex(new Vec3(-1, -1, +1), new Vec3(0, 255, 0), new Vec3(1, 0, 1)), // 4    //12
                new Vertex(new Vec3(-1, -1, +1), new Vec3(0, 255, 0), new Vec3(0, 0, 1)), // 4    //13
                new Vertex(new Vec3(-1, -1, +1), new Vec3(0, 255, 0), new Vec3(1, 0, 1)), // 4    //14

                new Vertex(new Vec3(+1, -1, +1), new Vec3(0, 0, 255), new Vec3(0, 0, 1)), // 5    //15
                new Vertex(new Vec3(+1, -1, +1), new Vec3(0, 0, 255), new Vec3(1, 0, 1)), // 5    //16
                new Vertex(new Vec3(+1, -1, +1), new Vec3(0, 0, 255), new Vec3(0, 0, 1)), // 5    //17

                new Vertex(new Vec3(+1, +1, +1), new Vec3(255, 0, 0), new Vec3(0, 1, 1)), // 6    //18
                new Vertex(new Vec3(+1, +1, +1), new Vec3(255, 0, 0), new Vec3(0, 0, 1)), // 6    //19
                new Vertex(new Vec3(+1, +1, +1), new Vec3(255, 0, 0), new Vec3(1, 0, 1)), // 6    //20

                new Vertex(new Vec3(-1, +1, +1), new Vec3(0, 255, 0), new Vec3(1, 1, 1)), // 7    //21
                new Vertex(new Vec3(-1, +1, +1), new Vec3(0, 255, 0), new Vec3(1, 0, 1)), // 7    //22
                new Vertex(new Vec3(-1, +1, +1), new Vec3(0, 255, 0), new Vec3(0, 0, 1))  // 7    //23
        };

        indexes = new Int3[]{
                new Int3(0, 3, 6),    //top
                new Int3(0, 6, 9),
                new Int3(21, 18, 15), //bot
                new Int3(21, 5, 12),
                new Int3(1, 10, 22),  //left
                new Int3(1, 22, 13),
                new Int3(7, 4, 16),   //right
                new Int3(7, 16, 19),
                new Int3(11, 8, 20),  //front
                new Int3(11, 20, 23),
                new Int3(5, 2, 14),   //back
                new Int3(5, 14, 17)
        };
    }
}
