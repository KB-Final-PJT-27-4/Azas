export interface Bank {
  code: string
  name: string
}

export interface AccountRegistrationForm {
  bankCode: string
  bankName: string
  accountNumber: string
  accountAlias: string
}

export interface AccountRegistrationRequest {
  bankCode: string
  accountNumber: string
  accountAlias: string
}

export interface AccountRegistrationResponse {
  accountId: number
  bankName: string
  accountNumber: string
  accountAlias: string
}
