package pl.jarekzegzula.contract;

import pl.jarekzegzula.system.exception.IllegalContractTypeArgument;

public enum ContractType {
  CONTRACT_OF_EMPLOYMENT(1),

  CONTRACT_OF_MANDATE(2),

  CONTRACT_B2B(3);

  private final Integer value;

  ContractType(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public static ContractType fromValue(Integer value) {

    for (ContractType contractType : values()) {
      if (contractType.value.equals(value)) {
        return contractType;
      }
    }
    throw new IllegalContractTypeArgument("Invalid ContractType value: " + value);
  }

  public static boolean isValidContractTypeValue(Integer value) {
    for (ContractType contractType : ContractType.values()) {
      if (contractType.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
