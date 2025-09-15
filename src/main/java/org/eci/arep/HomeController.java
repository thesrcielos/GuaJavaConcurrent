package org.eci.arep;

import org.eci.arep.annotations.GetMapping;
import org.eci.arep.annotations.RequestParam;
import org.eci.arep.annotations.RestController;

@RestController
public class HomeController {
   @GetMapping("/hello")
   public static String hello(@RequestParam(value = "name", defaultValue = "world") String name){
       return "greeting from microframework to " + name;
   }
}
