package business;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class Cat {
    private final String name;
    private final String breed;
    protected String photoId;
    protected String description;

    public Cat(@NotNull String name,
               @NotNull String breed) {
        this.name = name;
        this.breed = breed;

    }

    public Cat setPhotoId(){
        this.photoId =Arrays.stream(Objects.requireNonNull(new File(".\\cats\\" + getBreed() + "\\breeders\\" + getName()).list()))
                .filter(file -> file.startsWith("avatar"))
                .findFirst()
                .get()
                .split("\\.")[1];
        return this;
    }

    public Cat setDescription(){
        try {
            StringBuilder sb = new StringBuilder();
            this.description = sb.append(Files.readAllLines(Path.of(".\\cats\\" + getBreed() + "\\breeders\\"+ getName() + "\\desc.txt"))).substring(1, sb.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getBreed() {
        return breed;
    }

    public String getPhotoId() {
        return photoId;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cat cat = (Cat) o;
        return name.equals(cat.name) && breed.equals(cat.breed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(breed);
    }

    @Override
    public String toString() {
        return  "<b>Имя:</b> " + name + '\n' +
                "<b>Порода:</b> " + breed + "\n\n"+
                description;
    }
}
