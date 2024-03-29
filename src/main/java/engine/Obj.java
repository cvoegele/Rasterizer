package engine;

import util.Int3;
import util.Vec3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Obj extends Mesh {

    private Vec3 color;

    public Vec3 getColor() {
        return color;
    }

    public void setColor(Vec3 color) {
        this.color = color;
    }

    public Obj(String path, Vec3 color, ModelMatrixFunction modelMatrixFunction, Rasterizer rasterizer) throws FileNotFoundException {


        super(modelMatrixFunction, rasterizer);
        this.color = color;
        Scanner scanner = new Scanner(new File("./src/main/resources/" + path));
        List<Vertex> vertices = new ArrayList<>();
        List<Int3> indexes = new ArrayList<>();

        while (scanner.hasNext()) {
            var line = scanner.nextLine();
            var splits = line.split(" ");


            if (splits.length != 0) {
                if ("v".equals(splits[0])) {
                    var x = Float.parseFloat(splits[1]);
                    var y = Float.parseFloat(splits[2]);
                    var z = Float.parseFloat(splits[3]);
                    vertices.add(new Vertex(new Vec3(x, y, z), color, Vec3.ZERO));
                } else if ("f".equals(splits[0])) {
                    var i0 = Integer.parseInt(splits[1]) - 1;
                    var i1 = Integer.parseInt(splits[3]) - 1;
                    var i2 = Integer.parseInt(splits[2]) - 1;
                    indexes.add(new Int3(i0, i1, i2));
                } else {
                    System.out.println("line ignore: " + line);
                }
            }
        }
        this.vertices = vertices.toArray(Vertex[]::new);
        this.indexes = indexes.toArray(Int3[]::new);
    }


}
