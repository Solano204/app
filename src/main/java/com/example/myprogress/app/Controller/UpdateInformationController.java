package com.example.myprogress.app.Controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import com.example.myprogress.app.Entites.appUser;
import com.example.myprogress.app.Entites.infoLogged;
import com.example.myprogress.app.Exceptions.FieldIncorrectException;
import com.example.myprogress.app.Exceptions.UnsuccessfulRegisterException;
import com.example.myprogress.app.FeauturesServices.RecipesService;
import com.example.myprogress.app.FeauturesServices.RoutineService;
import com.example.myprogress.app.GeneralServices.GenerateResponse;
import com.example.myprogress.app.GeneralServices.GeneratorDataUser;
import com.example.myprogress.app.RedisService.TokenServices;
import com.example.myprogress.app.updateInformationService.caloriesIntakeService;
import com.example.myprogress.app.updateInformationService.updateInformationUserService;
import com.example.myprogress.app.validations.ValidationOnlyRegisterGroup;

import jakarta.validation.Valid;
import lombok.Data;
@Data 
@RequestMapping("/updateInformation")
@RestController
public class UpdateInformationController {

    private final GeneratorDataUser generatorDataUser;
    private final updateInformationUserService updateInformationUserService;
    private final caloriesIntakeService caloriesIntakeService;
    private final  TokenServices tokenServices;
    private final GenerateResponse generateResponse;
    private final RoutineService routineService;
    private final RecipesService recipesService;


    // This method change the user 
    @PostMapping("/ChangeData")
    public ResponseEntity<?> changeData(
            @Valid @RequestBody appUser user,
            @DefaultValue("notFound") @RequestParam String newUser) { // I put the app User because this is the entity
                                                                      // that has all attributes
        // (inclusive the password)
        user.setInfoLogged(new infoLogged());
        if (newUser.equals("notFound")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("El usuario esta vacio");
        }

        if (updateInformationUserService.changeUser(user, newUser)) {
            // Here I get the new information with the new user
            caloriesIntakeService.changeDocumentId(user.getUser(), newUser);
            routineService.updateUserInRoutines(user.getUser(), newUser);
            recipesService.updateUserInRecipes(user.getUser(), newUser);
            tokenServices.deleteToken(user.getUser());
            Map<String, Object> body = new HashMap<>();
            user.setUser(newUser);
            generateResponse.generateResponse(user, body);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The user couldn't be updated ");
    }

    // This method will execute each the user decide change its data (age, gender, height, goal, activity) that couuld affect the progress of the user
    @PostMapping("/updateData")
    public ResponseEntity<?> modifyDataSelected(
            @Validated(ValidationOnlyRegisterGroup.class) @RequestBody appUser user) {
        user.setInfoLogged(new infoLogged());
        if (updateInformationUserService.updateInformationUserRecommendedData(user)) {
            // Here I get the new information with the new user
            Map<String, Object> response = new HashMap<>();
            appUser appUser = updateInformationUserService.getDataUpdated(user.getUser(), user.getTypeAuthentication());
            response.put("user", appUser);
            response.put("ProgresObjetive", updateInformationUserService.evaluateObjetive(user));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The user couldn't be updated ");
    }

    // This method to change the password
    @PostMapping("/ChangePassword")
    public ResponseEntity<?> changePassword(
            @DefaultValue("notFound") @RequestParam String newPass,
            @DefaultValue("notFound") @RequestParam String user,
            @DefaultValue("notFound") @RequestParam String oldPass,
            @DefaultValue("notFound") @RequestParam String typeAuthentication) {

        if (user.equals("notFound") || typeAuthentication.equals("notFound") || oldPass.equals("notFound") || newPass.equals("notFound")) {
        return ResponseEntity.status(HttpStatus.CREATED).body("algun campo esta vacio ");
        }
        
        if (updateInformationUserService.changePassword(newPass, user, oldPass) != 0) {
            // Here I get the new information with the new user

            appUser appUser = updateInformationUserService.getDataUpdated(user, typeAuthentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(appUser);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("No se pudo cambiar el password ");
    }



    // This method to delete the user
    @DeleteMapping("/deleteUser")
    public ResponseEntity<?> deleteUser(
            @Valid @RequestBody appUser user) {
        if (updateInformationUserService.deleteUser(user)) {
            caloriesIntakeService.deleteById(user.getUser());
            tokenServices.deleteToken(user.getUser());
            routineService.deleteRoutinesByUser(user.getUser());
            recipesService.deleteRecipesByUser(user.getUser());
            // Here I get the new information with the new user
            return ResponseEntity.status(HttpStatus.OK)
                    .body("User deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("The user couldn't be deleted ");
    }

}