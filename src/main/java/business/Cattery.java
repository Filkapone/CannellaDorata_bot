package business;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class Cattery {
    private static final List<Cat> breeders = new ArrayList<>();
    private static final List<Kitten> kittens = new ArrayList<>();
    private static final String fileDir = "./cats";

    static{
        initBreeders();
        initKittens();
    }

    private static void initBreeders(){
        List<File> breedDirs;
        List<File> catDirs = new ArrayList<>();
        try {
            breedDirs = Files.list(Path.of(fileDir))
                    .map(file -> new File(file + "\\breeders\\"))
                    .filter(File::isDirectory)
                    .collect(Collectors.toList());
            for(File file : breedDirs){
                Files.list(file.toPath())
                        .forEach(element -> catDirs.add(new File(String.valueOf(element))));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for(File file : catDirs){
            String[] params = file.toString().split("\\\\");
            String name = params[params.length-1];
            String breed = params[params.length-3];

            breeders.add(new Cat(name, breed).setPhotoId().setDescription());
        }
    }

    private static void initKittens(){
        List<File> breedDirs;
        List<File> catDirs = new ArrayList<>();
        try {
            breedDirs = Files.list(Path.of(fileDir))
                    .map(file -> new File(file + "/kittens/"))
                    .filter(File::isDirectory)
                    .collect(Collectors.toList());
            for(File file : breedDirs){
                Files.list(file.toPath())
                        .forEach(element -> catDirs.add(new File(String.valueOf(element))));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for(File file : catDirs){
            String[] params = file.toString().split("\\\\");
            String name = params[params.length-1];
            String breed = params[params.length-3];

            kittens.add(new Kitten(name, breed).setPhotoId().setDescription());
        }
    }

    public static Optional<Cat> getCat(String name){
        for (Cat cat: breeders){
            if (cat.getName().equalsIgnoreCase(name)) return Optional.of(cat);
        }
        return Optional.empty();
    }

    public static Optional<Kitten> getKitten(String name){
        for (Kitten kitten : kittens){
            if(kitten.getName().equalsIgnoreCase(name)) return Optional.of(kitten);
        }
        return Optional.empty();
    }

    public static boolean isOurCat(String name){
        for(Cat cat : breeders){
            if(cat.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static List<Cat> getBreedCats(String breed){
        return breeders.stream()
                .filter(cat -> cat.getBreed().equals(breed))
                .collect(Collectors.toList());
    }

    public static List<Kitten> getAvailableKittens(){
        return kittens.stream()
                .filter(Kitten::isFree)
                .collect(Collectors.toList());
    }

    public static void addKitten(String name, String breed, String photoId, File photo){
        Kitten kitten = new Kitten(name, breed);
        kitten.setPhotoId(photoId);
        File photoDst = new File(fileDir + "/" + breed + "/kittens/" + name + "/avatar." + photo + ".txt");
        File description = new File(fileDir + "/" + breed + "/kittens/" + name + "/desc.txt");
        kitten.setDescription();
        kittens.add(kitten);
    }
}