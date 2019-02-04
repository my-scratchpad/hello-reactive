package io.iamkyu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Controller
public class ImageController {
    private static final String BASE_PATH = "/images";
    private static final String FILENAME = "{filename:.+}";

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/")
    public Mono<String> index(Model model) {
        model.addAttribute("images", imageService.getAll());
        return Mono.just("index");
    }

    @PostMapping(value = BASE_PATH)
    public Mono<String> createFile(@RequestPart(name = "file") Flux<FilePart> files) {
        return imageService.create(files)
                .then(Mono.just("redirect:/"));
    }

    @GetMapping(value = BASE_PATH + "/" + FILENAME + "/raw", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public Mono<ResponseEntity<?>> oneRawImage(@PathVariable String filename) {
        return imageService.getOne(filename)
                .map(resource -> {
                    try {
                        return ResponseEntity.ok()
                                .contentLength(resource.contentLength())
                                .body(new InputStreamResource(resource.getInputStream()));
                    } catch (IOException e) {
                        return ResponseEntity.badRequest()
                                .body(String.format("Couldn't find %s => %s", filename, e.getMessage()));
                    }
                });
    }


    @DeleteMapping(BASE_PATH + "/" + FILENAME)
    public Mono<String> deleteFile(@PathVariable String filename) {
        return imageService.delete(filename)
                .then(Mono.just("redirect:/"));
    }
}