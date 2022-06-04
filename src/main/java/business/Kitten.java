package business;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class Kitten extends Cat{
    private boolean isBooked;

    public Kitten(String name, String breed) {
        super(name, breed);
    }
    @Override
    public Kitten setPhotoId(){
        this.photoId = Arrays.stream(Objects.requireNonNull(new File(".\\cats\\" + getBreed() + "\\kittens\\" + getName()).list()))
                .filter(file -> file.startsWith("avatar"))
                .findFirst()
                .get()
                .split("\\.")[1];
        return this;
    }

    public void setPhotoId(String photoId){
        this.photoId = photoId;
    }

    public Kitten setDescription(){
        try {
            StringBuilder sb = new StringBuilder();
            this.description = sb.append(Files.readAllLines(Path.of(".\\cats\\" + getBreed() + "\\kittens\\"+ getName() + "\\desc.txt"))).toString().substring(1,sb.length()-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public boolean isBooked(){
        return isBooked;
    }
    public boolean isFree(){return !isBooked;}

    public void book(){
        this.isBooked = true;
        try {
            Files.createFile(Path.of(".\\cats\\"+getBreed()+"\\kittens\\"+getName()+"/"+".booked"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
