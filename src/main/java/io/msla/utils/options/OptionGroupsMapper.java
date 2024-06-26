package io.msla.utils.options;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.options.MSLAOptionGroup;
import io.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.HashMap;

public class OptionGroupsMapper {
    private final HashMap<MSLAOptionName, OptionGroup> optionGroups = new HashMap<>();
    private static OptionGroupsMapper instance;

    @Getter
    public static class OptionGroup implements MSLAOptionGroup {
        private final String name;
        private final Integer order;
        public OptionGroup(String name, Integer order) {
            this.name = name;
            this.order = order;
        }
    }

    public static OptionGroupsMapper getInstance() throws MSLAException {
         if (instance == null) instance = new OptionGroupsMapper();
         return instance;
    }

    private OptionGroupsMapper() throws MSLAException {
        var xmlReader = new SAXReader();
        try(var resource = getClass().getClassLoader().getResourceAsStream("options_map.xml")) {
            if (resource == null) throw new MSLAException("Options mapping file not found");
            var input = xmlReader.read(resource);
            var root = input.getRootElement();
            var it = root.elementIterator("group");
            while (it.hasNext()) {
                var groupElement = it.next();
                var groupName = groupElement.attributeValue("name");
                if (groupName == null) throw new MSLAException("Group must have a 'name' attribute");
                var order = groupElement.attributeValue("order");
                var group = new OptionGroup(groupName, order != null ? Integer.parseInt(order) : 99999);
                var it2 = groupElement.elementIterator("option");
                while (it2.hasNext()) {
                    var optionElement = it2.next();
                    var optionName = optionElement.attributeValue("name");
                    if (optionName == null) throw new MSLAException("Option must have a 'name' attribute");
                    optionGroups.put(MSLAOptionName.valueOf(optionName), group);
                }
            }
        } catch (DocumentException | IOException e) {
            throw new MSLAException("Error reading options mapping XML file", e);
        }
    }

    public final MSLAOptionGroup getGroup(MSLAOptionName option) { return optionGroups.get(option); }
}
