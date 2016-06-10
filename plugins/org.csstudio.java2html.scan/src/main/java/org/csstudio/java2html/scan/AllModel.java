package org.csstudio.java2html.scan;

import java.util.List;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Property;


@Model(className = "AllData", properties = {
        @Property(name = "someInt", type = int.class), 
        @Property(name = "someArray", type = AllModel.SomeArrayModel.class, array = true)}, targetId = "")
public class AllModel {
    private static AllData ui;


    public static void onPageLoad() throws Exception {
        
        ui = new AllData();
        ui.setSomeInt(5);
        ui.getSomeArray().add(new SomeArrayData(1,"test"));
        ui.applyBindings();
       
    }
    
    @ComputedProperty
    public static int computedProp(List<SomeArrayData> someArray){
    	return 5;
    }
    
    @Model(className = "SomeArrayData", properties = {
            @Property(name = "id", type = long.class, mutable = false),
            @Property(name = "name", type = String.class, mutable = false)
        }, targetId = "")
    public class SomeArrayModel {

    }
}
