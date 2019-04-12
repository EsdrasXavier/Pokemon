package br.org.catolicasc.pokemon;

public class Pokemon {

    private int id;
    private int num;
    private String name;
    private String imgUrl;

    Pokemon() { }

    Pokemon(int id, int num, String name, String imgUrl) {
        this.id = id;
        this.num = num;
        this.name = name;
        this.imgUrl = imgUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
