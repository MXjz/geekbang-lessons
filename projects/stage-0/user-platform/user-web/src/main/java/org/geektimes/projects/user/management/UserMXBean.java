package org.geektimes.projects.user.management;

import javax.management.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态结构, 无固定结构类型
 * @author xuejz
 * @description
 * @Time 2021/3/14 21:48
 */
public class UserMXBean implements DynamicMBean {

    // 五个属性, 动态维护
    // 适用于没有固定结构的属性,方便扩展
    // id, name, password, email, phoneNumber
    private Map<String, Object> attributeMap = new HashMap<>();

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (!attributeMap.containsKey(attribute)) {
            throw new AttributeNotFoundException("Attribute not found!");
        }
        return attributeMap.get(attribute);
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        attributeMap.put(attribute.getName(), attribute.getValue());
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList attributeList = new AttributeList();
        for(String attributeName : attributes) {
            Object attributeVal;
            try {
                attributeVal = getAttribute(attributeName);
                attributeList.add(new Attribute(attributeName, attributeVal));
            } catch (AttributeNotFoundException | MBeanException | ReflectionException  e) {
            }
        }
        return attributeList;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {

        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return null;
    }
}
