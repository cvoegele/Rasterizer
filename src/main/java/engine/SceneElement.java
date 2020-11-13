package engine;

public interface SceneElement {
    void addChild(Mesh mesh);

    void removeChild(Mesh mesh);

    SceneElement[] getChildren();

    void setDepth(int depth);

    int getDepth();
}
