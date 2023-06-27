package futurelink.msla.ui;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;

public class ProjectPanel extends JPanel {
    private JList<String> projectList;

    private record ProjectListModel(Project project) implements ListModel<String> {
        @Override
            public int getSize() {
                return project.getLayersCount() + 1;
            }

            @Override
            public String getElementAt(int index) {
                if (index == 0) return "Settings";
                else return project.getLayerName(index-1);
            }

            @Override
            public void addListDataListener(ListDataListener l) {

            }

            @Override
            public void removeListDataListener(ListDataListener l) {

            }
        }
    public ProjectPanel() {
        setLayout(new GridLayout(1, 1));

        projectList = new JList<>();
        setMinimumSize(new Dimension(200, 100));

        var scrollPane = new JScrollPane(projectList);
        add(scrollPane);
    }

    public void setProject(Project project) {
        var projectListModel = new ProjectListModel(project);
        projectList.setModel(projectListModel);
    }

}
