package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Student;
import models.StudentStore;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Utils;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StudentController extends Controller {
    final private HttpExecutionContext context;
    final private StudentStore store;

    @Inject
    public StudentController(HttpExecutionContext context, StudentStore store) {
        this.context = context;
        this.store = store;
    }

    public CompletionStage<Result> create(Http.Request request) {
        JsonNode json = request.body().asJson();

        return CompletableFuture.supplyAsync(() -> {
            if(json == null) {
                return badRequest(Utils.createResponse("Expecting JSON data", false));
            }
            Optional<Student> studentOptional = store.addStudent(Json.fromJson(json, Student.class));

            return studentOptional.map(student -> {
                JsonNode jsonObject = Json.toJson(student);
                return created(Utils.createResponse(jsonObject, true));
            }).orElse(internalServerError(Utils.createResponse("Could not create data", false)));
        }, context.current());

    }

    public CompletionStage<Result> update(Http.Request request) {
        JsonNode json = request.body().asJson();

        return CompletableFuture.supplyAsync(() -> {
            if(json == null) {
                return badRequest(Utils.createResponse("Expecting JSON data", false));
            }
            Optional<Student> studentOptional = store.updateStudent(Json.fromJson(json, Student.class));
            return studentOptional.map(student -> {
                if(student == null) {
                    return notFound(Utils.createResponse("Student not found", false));
                }
                JsonNode jsonObject = Json.toJson(student);
                return ok(Utils.createResponse(jsonObject, true));
            }).orElse(internalServerError(Utils.createResponse("Could not create data", false)));
        }, context.current());
    }

    public CompletionStage<Result> retrieve(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Student> studentOptional = store.getStudent(id);
            return studentOptional.map(student -> {
                JsonNode jsonObject = Json.toJson(student);
                return ok(Utils.createResponse(jsonObject, true));
            }).orElse(internalServerError(Utils.createResponse("Student with id:" + id + " not found", false)));
        }, context.current());
    }

    public CompletionStage<Result> delete(int id) {
        return CompletableFuture.supplyAsync(() -> {
            boolean status = store.deleteStudent(id);
            if(!status) {
                return notFound(Utils.createResponse("Student with id: " + id + " not found", false));
            }
            return ok("Student with id: " + id + " deleted");
        }, context.current());
    }

    public CompletionStage<Result> listStudents() {
        return CompletableFuture.supplyAsync(() -> {
            Set<Student> students = store.getStudents();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonObject = mapper.convertValue(students, JsonNode.class);
            return ok(Utils.createResponse(jsonObject, true));
        }, context.current());
    }
}
