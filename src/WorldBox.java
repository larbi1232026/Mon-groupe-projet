public class WorldBox extends AbstractBox {

    private World innerWorld;

    public WorldBox(Position position, World innerWorld) {
        super(position);
        this.innerWorld = innerWorld;
    }

    public World getInnerWorld() {
        return innerWorld;
    }
}
