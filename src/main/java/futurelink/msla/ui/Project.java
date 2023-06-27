package futurelink.msla.ui;

import java.util.HashMap;

public class Project {
    private final HashMap<String, ProjectLayer> layers = new HashMap<>();

    public Project() {
        layers.put("Copper Front", new ProjectLayer());
        layers.put("Copper Back", new ProjectLayer());
        layers.put("Mask Front", new ProjectLayer());
        layers.put("Mask Back", new ProjectLayer());
    }

    public int getLayersCount() {
        return layers.size();
    }

    public String getLayerName(int index) {
        return (String) layers.keySet().stream().sorted().toArray()[index];
    }
}
