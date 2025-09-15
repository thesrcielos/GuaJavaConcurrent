package org.eci.arep;

import org.eci.arep.annotations.GetMapping;
import org.eci.arep.annotations.RequestParam;
import org.eci.arep.annotations.RestController;

@RestController
public class TempController {

    @GetMapping("/temp")
    public static String temp(@RequestParam(value = "msg", defaultValue = "default") String msg) {
        return "Temp says: " + msg;
    }
}