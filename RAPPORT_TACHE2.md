# Tâche 2 : Tests unitaires Automatiques

**Autrices :** Chaimaa Dannane && Ines Amelia Chafai

**Date :** 7 octobre 2025

**Structure du projet:**

**Partie 1:** Ce rapport présente une partie de la tâche 2. Cette partie couvre les 5 tests (dont 1 java-faker) dans le **module** `reader-gtfs` sur la **classe** `com.graphhopper.gtfs.fare.Fares`

**Partie 2:** L'autre partie est consultable dans le document accessible ici [lien]

---

## Table des matières

1. [Classe sélectionnée](#1-classe-sélectionnée)
2. [Configuration du projet](#2-configuration-du-projet)
3. [Analyse de mutation AVANT les nouveaux tests](#3-analyse-de-mutation-avant-les-nouveaux-tests)
4. [Documentation des 7 nouveaux tests](#4-documentation-des-7-nouveaux-tests)
5. [Analyse de mutation APRES les nouveaux tests](#5-analyse-de-mutation-après-les-nouveaux-tests)
6. [Exécution et validation](#6-exécution-et-validation)
---

## 1. Classe sélectionnée


**Classe :** `Fares.java`  
**Package :** `com.graphhopper.gtfs.fare`  
**Chemin :** [reader-gtfs/src/main/java/com/graphhopper/gtfs/fare/Fares.java](reader-gtfs/src/main/java/com/graphhopper/gtfs/fare/Fares.java)

- **Couverture de code original** : 98% (43/44 lignes)
- **Score de mutation initial** : 77% (23/30 mutants tués)
- **7 mutants survivants identifiés** : Possibilité de créer plusieurs tests ciblés


---

## 2. Configuration du projet

### 2.1 Ajout de PiTest

**Configuration dans [reader-gtfs/pom.xml](reader-gtfs/pom.xml) :**

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.17.1</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetClasses>
            <param>com.graphhopper.reader.gtfs.*</param>
            <param>com.graphhopper.gtfs.*</param>
        </targetClasses>
        <targetTests>
            <param>com.graphhopper.gtfs.*</param>
            <param>com.graphhopper.reader.gtfs.*</param>
        </targetTests>
        <outputFormats>
            <param>HTML</param>
            <param>XML</param>
        </outputFormats>
        <timestampedReports>false</timestampedReports>
        <mutationThreshold>30</mutationThreshold>
        <coverageThreshold>0</coverageThreshold>
        <timeoutConstant>3000</timeoutConstant>
        <verbose>true</verbose>
        <threads>2</threads>
        <failWhenNoMutations>false</failWhenNoMutations>
                    <jvmArgs>
                        <value>-Xmx1200m</value>
                        <value>-Xms1200m</value>
                    </jvmArgs>
                    <useClasspathFile>true</useClasspathFile>
    </configuration>
</plugin>
```

### 2.2 Ajout de java-faker

**Dépendance ajoutée dans [reader-gtfs/pom.xml](reader-gtfs/pom.xml) :**

```xml
<dependency>
    <groupId>com.github.javafaker</groupId>
    <artifactId>javafaker</artifactId>
    <version>1.0.2</version>
    <scope>test</scope>
</dependency>
```

---

## 3. Analyse de mutation AVANT les nouveaux tests

**Commande :**
```bash
cd reader-gtfs
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

**Rapport PiTest :** [rapport-pitest-avant/index.html](reader-gtfs/rapport-pitest-avant/index.html)

![Module AVANT](reader-gtfs/Screenshots/Module_AVANT.png)

![Fare AVANT](reader-gtfs/Screenshots/Fare_AVANT.png)

![Fares.java AVANT](reader-gtfs/Screenshots/Fares_AVANT.png)


![Détail des mutations](reader-gtfs/Screenshots/Mutations_AVANT1.png)

![Détail des mutations](reader-gtfs/Screenshots/Mutations_AVANT2.png)

---

## 4. Documentation des 5 nouveaux tests

**Fichier créé :** [reader-gtfs/src/test/java/com/graphhopper/gtfs/fare/NewFareTest.java](reader-gtfs/src/test/java/com/graphhopper/gtfs/fare/NewFareTest.java)

### Test 1 : `AppliesReturnTrueWhenFareHasNoRulesTest`

**Mutant ciblé :** Ligne 92 - `replaced boolean return with false for lambda$applies$4`

**Intention du test :**  
Vérifier que la méthode `applies()` retourne `true` quanf `fare_rules` est vide. Une fare sans règle doit s'appliquer à tous les segments.

**Données de test :**
- **Fare :** `fare_id = "test_fare"`,  liste vide
- **Segment :** `feed_id = "feed_1"`, `route = "Route_001"`, stations quelconques, pas de zones

**Oracle :**  
`possibleFares(map(fare), segment)` doit contenir la `fare` testée.

---

### Test 2 : `SanititizeFareRulesCreatesOriginDestinationRuleWhenBothIdsPresentTest`

**Mutants ciblés :** Ligne 99 - `negated conditional` (pour `origin_id` et `destination_id`)

**Intention du test :**  
Vérifier que `sanitizeFareRules()` crée une règle d'origine-destination `OriginDestinationRule` lorsque les deux IDs `origin_id` et `destination_id sont présents dans une FareRule.

**Données de test :**
- **FareRule :** `origin_id = "zoneA"`, `destination_id = "zoneB"`, autres champs = `null`

**Oracle :**  
La liste doit contenir exactement 1 `OriginDestinationRule`. Si l'un des conditionnels est inversé, le filtre échoue et aucune règle n'est créée.

---

### Test 3 : `SanitizefareRulesCreatesZoneRuleWhenContainsIdPresentTest`

**Mutants ciblés :** Ligne 100 - `negated conditional`, `replaced boolean return with true`

**Intention du test :**  
Vérifier que `sanitizeFareRules()` crée une `ZoneRule` qui regroupe toutes les zones (`contains_id`) présents dans les règles `FareRule`.

**Données de test :**
- **2 FareRule :** `contains_id = "zoneA"` et `contains_id = "zoneB"`

**Oracle :**  
La liste doit contenir exactement 1 `ZoneRule` (les 2 zones sont regroupées dans un seul objet). Si le conditionnel est inversé, aucune zone ne sera collectée.

---

### Test 4 : `SanitizeFareRulesCreatesEmptyZoneRuleWhenNoContainsIdTest`

**Mutant ciblé :** Ligne 100 - `replaced return value with ""` (dans lambda qui mappe `contains_id`)

**Intention du test :**  
Vérifier que `sanitizeFareRules()` crée une règle de zone `ZoneRule` même lorsque `contains_id` est absent dans une `FareRule`.

**Données de test :**
- **FareRule :** `route_id = "Route_000"`, `contains_id = null`

**Oracle :**  
La liste doit contenir 1 `ZoneRule` (avec une zone vide). Si le mutant remplace la valeur par une chaîne vide, cela pourrait affecter la création de la règle.

---

### Test 5 : `SanitizeFareRulesNeverReturnsEmptyListTest`

**Mutant ciblé :** Ligne 101 - `replaced return value with Collections.emptyList`

**Intention du test :**  
Vérifier que `sanitizeFareRules()` ne retourne **jamais** une liste vide même avec des données aléatoires.

**Données de test (générées avec JavaFaker) :**
- **FareRule 1 :** `route_id = faker.regexify("Route_[0-9]{3}")` → Ex: `"Route_123"`
- **FareRule 2 :** `origin_id = faker.address().cityName()`, `destination_id = faker.address().cityName()` → Ex: `"Montréal"`, `"Québec"`
- **FareRule 3 :** `contains_id = faker.regexify("zone[A-Z]")` → Ex: `"zoneC"`

**Oracle :**  
La liste retournée ne doit **jamais** être vide (`resultat.isEmpty() == false` ET `resultat.size() >= 1`). Le code ajoute toujours au minimum une `ZoneRule`, donc même avec des données aléatoires, la liste doit contenir au moins un élément.

En simulant des données réalistes, la couverture du code est augmentée grâce au caractère aléatoire des fakers. 

---

## 5. Analyse de mutation APRES les nouveaux tests

**Commande :**
```bash
cd reader-gtfs
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

- **Line Coverage :** 98% (43/44)
- **Mutation Coverage :** 87% (26/30)
- **Test Strength :** 87% (26/30)

**Amélioration :**
- Mutants tués avant : 23/30 (77%)
- Mutants tués après : 26/30 (87%)

**Rapport PiTest :** [rapport-pitest-apres/index.html](reader-gtfs/rapport-pitest-apres/index.html)

![Module APRES](reader-gtfs/Screenshots/Module_APRES.png)

![Fare APRES](reader-gtfs/Screenshots/Fare_APRES.png)

![Fares.java APRES](reader-gtfs/Screenshots/Fares_APRES.png)


| Ligne | Mutant | Statut AVANT | Statut APRES | Mon test |
|-------|--------|--------------|--------------|----------|
| 92 | `replaced boolean return with false` | SURVIVED | SURVIVED | Test 1 |
| 99 | `negated conditional` (origin) | SURVIVED | KILLED| Test 2 |
| 99 | `negated conditional` (destination) | SURVIVED | KILLED | Test 2 |
| 100 | `replaced boolean return with true` | SURVIVED | SURVIVED | Test 3 |
| 100 | `negated conditional` | SURVIVED | SURVIVED | Test 3 |
| 100 | `replaced return with ""` | SURVIVED | SURVIVED | Test 4 |
| 101 | `replaced return with emptyList` | SURVIVED | KILLED | Test 5 |

![Détail des mutations](reader-gtfs/Screenshots/Mutations_APRES.png)


#### Mutants ligne 99 (Test 2)
Le test vérifie qu'une `OriginDestinationRule` est créée quand les 2 IDs sont présents. Si l'un des conditionnels est inversé (mutant), le filtre `origin_id != null && destination_id != null` échoue. Et donc `assertEquals(1, count)` ne trouve aucune règle.

#### Mutant ligne 101 (Test 5) 
Le test 5 vérifie que le résultat n'est jamais vide. Le mutant remplace le retour par `Collections.emptyList()`, ce qui fait échouer `assertFalse(resultat.isEmpty())` et `assertTrue(resultat.size() >= 1)`.

---

## 6. Exécution et validation

### 6.1 Tests unitaires

**Commande d'exécution des nouveaux tests :**
```bash
cd reader-gtfs
mvn test -Dtest=NewFareTest
```

![Résultat tests](reader-gtfs/Screenshots/Tests.png)

### 6.2 Exécution avec mutation testing

**Commande :**
```bash
mvn clean test org.pitest:pitest-maven:mutationCoverage
```
**Rapport PiTest AVANT:** [rapport-pitest-avant/terminal-output.tx](reader-gtfs/rapport-pitest-avant/terminal-output.txt)

**Rapport PiTest APRES:** [rapport-pitest-avant/terminal-output.tx](reader-gtfs/rapport-pitest-avant/terminal-output.txt)

---

## Annexes

### Fichiers sources

- **Classe testée :** [Fares.java](reader-gtfs/src/main/java/com/graphhopper/gtfs/fare/Fares.java)
- **Tests originaux :** [FareTest.java](reader-gtfs/src/test/java/com/graphhopper/gtfs/fare/FareTest.java)
- **Nouveaux tests :** [NewFareTest.java](reader-gtfs/src/test/java/com/graphhopper/gtfs/fare/NewFareTest.java)
- **Configuration Maven :** [pom.xml](reader-gtfs/pom.xml)

### Rapports PiTest

- **Rapport AVANT :** [rapport-pitest-avant/index.html](reader-gtfs/rapport-pitest-avant/index.html)
- **Rapport APRES :** [rapport-pitest-apres/index.html](reader-gtfs/rapport-pitest-apres/index.html)
- **Sortie terminal AVANT :** [rapport-pitest-avant/terminal-output.txt](reader-gtfs/rapport-pitest-avant/terminal-output.txt)
- **Sortie terminal APRES :** [rapport-pitest-apres/terminal-output.txt](reader-gtfs/rapport-pitest-apres/terminal-output.txt)

---
