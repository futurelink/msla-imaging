package futurelink.msla.ui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class PrinterSelectPanel extends JPanel {
    public PrinterSelectPanel() {
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        var printerChoice = new JComboBox<String>();
        printerChoice.addItem("123");
        printerChoice.addItem("123dsfsd");
        add(printerChoice);
    }
}
