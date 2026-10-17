## Buttons

Default button: 
![default button](images/Buttons/default%20button.png)

Regular Button: 
![regular button](images/Buttons/regular%20button.png)

A regular button is made simply by creating any default HTML element such as

```
<input type="submit"/>
<input type="reset"/>
<button></button>
```

A default button is made by adding the class "default":

```
<input type="submit" class="default"/>
<input type="reset" class="default"/>
<button class="default"></button>
```

If you want to imply urgency, you can make the button hop up and down by adding the class "pulse".  You should only use the pulse class in conjunction with the default class.

```
<input type="submit" class="default pulse"/>
<input type="reset" class="default pulse"/>
<button class="default pulse"></button>
```

---

_Source: [Buttons](https://sourceforge.net/p/skylightos/wiki/Buttons/) on the SourceForge wiki · Labels: button, UI · Last modified 2014-01-21_
