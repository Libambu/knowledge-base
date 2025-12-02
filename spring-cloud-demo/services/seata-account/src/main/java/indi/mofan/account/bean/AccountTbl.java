package indi.mofan.account.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class AccountTbl implements Serializable {

    @Serial
    private static final long serialVersionUID = -6999871158713125886L;

    private Integer id;

    private String userId;

    private Integer money;

}