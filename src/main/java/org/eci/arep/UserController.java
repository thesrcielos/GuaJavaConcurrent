package org.eci.arep;

import org.eci.arep.annotations.GetMapping;
import org.eci.arep.annotations.RequestParam;
import org.eci.arep.annotations.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    private static final Map<String, Double> users = new HashMap<>();
    @GetMapping("/users")
    public static String saveUser(@RequestParam("name") String name, @RequestParam("height") double height){
        users.put(name, height);
        return "User " + name + " with height " + height + " was saved";
    }

    @GetMapping("/users/coincidences")
    public static String getUser(@RequestParam("name") String name){
        Double height = users.get(name);
        return "User "+ name + " retrieved value: " + (height != null? "height is " + height : " user data not found");
    }
}

