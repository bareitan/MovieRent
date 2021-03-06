package com.example.bareitan.movierent;

/**
 * Created by bareitan on 01/04/2017.
 */

public class Category {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String name;
    private int id;

    public Category(int id, String name){
        this.name = name;
        this.id = id;
    }
    public Category(){

    }
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        Category cat = (Category)obj;
        if(cat.name.equals(this.name)){
            return true;
        }else{
            return false;
        }
    }
}
