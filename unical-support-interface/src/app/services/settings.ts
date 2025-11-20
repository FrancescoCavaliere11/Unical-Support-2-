import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class Settings {

  //TODO in futuro si può salvar eil valore impostato
  // nel localstorage per fare in modo di mantenere
  // il tema scelto dall'utente ad ogni avvio

  private _darkMode: boolean = true;

  constructor() { }

  get darkMode() {
    return this._darkMode;
  }

  set darkMode(value: boolean) {
    this._darkMode = value;
  }
}
