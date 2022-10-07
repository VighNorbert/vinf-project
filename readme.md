# VINF projekt - Norbert Vígh

## Téma: Vytvorenie rodokmeňu z dát z Wikipédie a vyhľadávanie nad ním

### 1. Zámer

V projekte sa budem venovať vyparsovaniu osôb z dát z anglickej Wikipédie, pričom získané osoby sa následne spoja do rodokmeňov vo forme grafu. Zároveň bude pri každej hrane zapamätaný vzťah medzi danými osobami.

Po spracovaní sa bude dať vyhľadať konkrétnu osobu, základné informácie o nej, bude sa dať taktiež zobraziť rodokmeň tejto osoby.
Okrem iného sa bude dať aj zistiť, či ľubovoľné dve osoby sú v rodinnom vzťahu a prípadne aj v akom.

Projekt bude naprogramovaný v jazyku **Java**. Framework zatiaľ nebol zvolený.

#### 1.1. Dáta
Dáta budú pochádzať z [anglickej Wikipédie](https://dumps.wikimedia.org/enwiki/latest/).
Dáta článkov z Wikipédie sú vo formáte XML.

* Každý tag `<page>` obsahuje informáciu práve o jednej stránke wiki.
* Zaujímavými tagmi sú tag `<title>`, ktorý obsahuje názov stránky, a tag `<text>`, ktorý obsahuje všetok hlavný text článku, ktorý sa zobrazí.
* Prelinkovanie medzi jednotlivými článkami je realizované prostredníctvom dvojitých hranatých zátvoriek.
    * Štandardný interný link obsahuje v zátvorkách title článku, na ktorý sa odkazuje.
    * Okrem toho môže byť názov linku aj upravený, pomocou tzv. pipe. `Title článku|Title odkazu`

Tieto základné koncepty by mali postačovať na jednoduché vyparsovanie rodokmeňu. 
Vzťah medzi dvomi ľuďmi by mal byť jednoducho vyparsovateľný z infoboxu stránky tejto osoby.
V prípade ak sa ukáže, že parsovanie z infoboxu nebude dostatočné, je možné, že budeme musieť vyhľadávať aj v hlavnom texte článku pomocou existujúcich odkazov, vety v ktorej sa ten odkaz nachádza, a prípadne blízkeho okolia tej vety.