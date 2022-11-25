public class Settings {
    private String server_ip = "192.168.56.1";   // 伺服器IP
    private int port = 8080;     // 伺服器port

    private String reg_name;
    private String reg_gender;
    private String reg_account;
    private String reg_pwd;
    private String reg_email;
    private String reg_phone;
    private String reg_pet_id;

    public String Get_IP(){
        return this.server_ip;
    }

    public int Get_Port(){
        return this.port;
    }

    public void Set_reg_name(String name){
        reg_name = name;
    }

    public void Set_reg_gender(String gender){
        reg_gender = gender;
    }

    public void Set_reg_account(String account){
        reg_account = account;
    }

    public void Set_reg_pwd(String pwd){
        reg_pwd = pwd;
    }

    public void Set_reg_email(String email){
        reg_email = email;
    }

    public void Set_reg_phone(String phone){
        reg_phone = phone;
    }

    public void Set_reg_pet_id(String id){
        reg_pet_id = id;
    }

    public String Get_reg_name(){
        return this.reg_name;
    }

    public String Get_reg_gender(){
        return this.reg_gender;
    }

    public String Get_reg_account(){
        return this.reg_account;
    }

    public String Get_reg_pwd(){
        return this.reg_pwd;
    }

    public String Get_reg_email(){
        return this.reg_email;
    }

    public String Get_reg_phone(){
        return this.reg_phone;
    }

    public String Get_reg_pet_id(){
        return this.reg_pet_id;
    }
}
