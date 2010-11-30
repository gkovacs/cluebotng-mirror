package main

import (
	"mysql"
	"http"
	"fmt"
	"strconv"
	"strings"
	"os"
	"bytes"
)

type pipelineStep struct {
	input chan *pipelinePackage
	quit chan int
}

type pipelinePackage struct {
	id int
	page int
	user string
	comment string
	nextUser string
	nextId int
	nextComment string
	delta int
	origSize int
}

func getMysql() (db *mysql.MySQL) {
	db = mysql.New()
	db.Connect("sql-s1", "cobi", MySQLPassword, "enwiki_p")
	return db
}

func filter(needDb bool, decision func(id *pipelinePackage, db *mysql.MySQL) bool, next pipelineStep) pipelineStep {
	input := make(chan *pipelinePackage)
	quit := make(chan int)
	step := pipelineStep{input, quit}
	output := next.input
	var db *mysql.MySQL
	if needDb {
		db = getMysql()
	} else {
		db = nil
	}
	go func() {
		for {
			select {
			case <-quit:
				next.quit <- 1
				return
			case id := <-input:
				if decision(id, db) {
					output <- id
				}
			}
		}
	}()
	return step
}

func branch(needDb bool, decision func(id *pipelinePackage, db *mysql.MySQL) bool, branch1, branch2 pipelineStep) pipelineStep {
	input := make(chan *pipelinePackage)
	quit := make(chan int)
	step := pipelineStep{input, quit}
	output1 := branch1.input
	output2 := branch2.input
	var db *mysql.MySQL
	if needDb {
		db = getMysql()
	} else {
		db = nil
	}
	go func() {
		for {
			select {
			case <-quit:
				branch1.quit <- 1
				branch2.quit <- 1
				return
			case id := <-input:
				if decision(id, db) {
					output1 <- id
				} else {
					output2 <- id
				}
			}
		}
	}()
	return step
}

func rangeGenerator(start, end int, next pipelineStep) {
	for iter := start ; iter < end ; iter++ {
		pkg := new(pipelinePackage)
		pkg.id = iter
		next.input <- pkg
	}
	next.quit <- 1
}

func outputSink(format string) pipelineStep {
	input := make(chan *pipelinePackage)
	quit := make(chan int)
	step := pipelineStep{input, quit}
	go func() {
		for {
			select {
			case <-quit:
				return
			case id := <-input:
				fmt.Printf(format, id.id)
			}
		}
	}()
	return step
}

func getURL(url string) (data string, error os.Error) {
	resp, _, err := http.Get(url)
	if err != nil {
		return "", err
	}
	respData := make([]byte, 2097152)
	n, _ := resp.Body.Read(respData)
	resp.Body.Close()
	data = bytes.NewBuffer(respData[0:n]).String()
	return data, nil
}

func main() {
	rangeGenerator(1, 500,
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If edit is in ns0, return true.
		res, err := db.Query("SELECT `page_namespace`, `page_id`, `rev_user_text`, `rev_comment` FROM `revision` JOIN `page` ON `rev_page` = `page_id` WHERE `rev_id` = " + strconv.Itoa(id.id))
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		if row[0] == 0 {
			id.page, _ = row[1].(int)
			id.user, _ = row[2].(string)
			id.comment, _ = row[3].(string)
			return true
		}
		return false
	},
	branch(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If edit was reverted, return true.
		res, err := db.Query("SELECT `rev_id`, `rev_user_text`, `rev_comment` FROM `revision` WHERE `rev_page` = " + strconv.Itoa(id.page) + " AND `rev_id` > " + strconv.Itoa(id.id) + " ORDER BY `rev_id` DESC LIMIT 1")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		id.nextId, _ = row[0].(int)
		id.nextUser, _ = row[1].(string)
		id.nextComment, _ = row[2].(string)
		if (strings.Contains(id.nextComment, "Revert") || strings.Contains(id.nextComment, "Undid")) && (strings.Contains(id.nextComment, id.user) || strings.Contains(id.nextComment, strconv.Itoa(id.id))) {
			if strings.Contains(id.nextUser, "Bot") {
				return true
			}
		}
		return false
	},
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If user was warned, return true.
		res, err := db.Query("SELECT `rev_id` FROM `revision` JOIN `page` ON `page_id` = `rev_page` WHERE `page_namespace` = 3 AND `page_title` = '" + db.Escape(strings.Replace(id.user, " ", "_", -1)) + "' AND `rev_user_text` = '" + db.Escape(id.nextUser) + "' AND (`rev_comment` LIKE '%warning%' OR `rev_comment` LIKE 'General note: Nonconstructive%') LIMIT 1")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		return true
	},
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If user still has warning, return true.
		data, err := getURL("http://en.wikipedia.org/w/index.php?action=raw&title=User_talk:" + http.URLEscape(id.user))
		if err != nil {
			return false
		}
		if strings.Contains(data, "<!-- Template:uw-") && strings.Contains(data, id.nextUser) {
			return true
		}
		return false
	},
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If warner has >300 edits, return true.
		res, err := db.Query("SELECT `user_editcount` FROM `user` WHERE `user_name` = '" + db.Escape(id.nextUser) + "'")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		editCount, _ := row[0].(int)
		if editCount > 300 {
			return true
		}
		return false
	},
	outputSink("%d V\n")))),

	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If user has no warnings on talk page, return true.
		data, err := getURL("http://en.wikipedia.org/w/index.php?action=raw&title=User_talk:" + http.URLEscape(id.user))
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		if err != nil {
			return false
		}
		if strings.Contains(data, "<!-- Template:uw-") {
			return false
		}
		return true
	},
	branch(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If user has more than 100 edits, return true.
		res, err := db.Query("SELECT `user_editcount` FROM `user` WHERE `user_name` = '" + db.Escape(id.user) + "'")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		editCount, _ := row[0].(int)
		if editCount > 100 {
			return true
		}
		return false
	},
	outputSink("%d C\n"),
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If page has more than 8 revisions since, return true.
		res, err := db.Query("SELECT COUNT(*) FROM `revision` WHERE `rev_page` = " + strconv.Itoa(id.page) + " AND `rev_id` > " + strconv.Itoa(id.id))
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		count, _ := row[0].(int)
		if count > 8 {
			return true
		}
		return false
	},
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If any of the next 8 edits are reverts, return false.
		res, err := db.Query("SELECT COUNT(*) FROM (SELECT `rev_comment` FROM `revision` WHERE `rev_page` = " + strconv.Itoa(id.page) + " AND `rev_id` > " + strconv.Itoa(id.id) + " LIMIT 8) AS `temp` WHERE `rev_comment` LIKE '%revert%' OR `rev_comment` LIKE 'Undid%'")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		count, _ := row[0].(int)
		if count > 0 {
			return false
		}
		return true
	},
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If edit resulted in increase in page length, and next 4 edits resulted in decrease, return false.
		res, err := db.Query("SELECT `a`.`rev_len` - `b`.`rev_len`, `b`.`rev_len` FROM `revision` AS `a` JOIN `revision` AS `b` ON `a`.`rev_parent_id` = `b`.`rev_id` WHERE `a`.`rev_id` >= " + strconv.Itoa(id.id) + " AND `a`.`rev_page` = " + strconv.Itoa(id.page) + " ORDER BY `a`.`rev_id` ASC LIMIT 5")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		row := res.FetchRow()
		if row == nil {
			return false
		}
		id.delta, _ = row[0].(int)
		id.origSize, _ = row[1].(int)
		if id.delta <= 0 {
			return true
		}
		for {
			row := res.FetchRow()
			if row == nil {
				break
			}
			diff, _ := row[0].(int)
			if diff < 0 {
				return false
			}
		}
		return true
	},
	filter(true, func(id *pipelinePackage, db *mysql.MySQL) bool {
		//If edit resulted in decrease in page length more than 500 bytes, and next 8 edits brought it back to within 10 bytes of original, return false.
		if id.delta >= 0 {
			return true
		}

		res, err := db.Query("SELECT `rev_len` FROM `revision` WHERE `rev_id` > " + strconv.Itoa(id.id) + " AND `rev_page` = " + strconv.Itoa(id.page) + " ORDER BY `a`.`rev_id` ASC LIMIT 8")
		if err != nil {
			fmt.Printf("Error: %v\n", err)
			return false
		}
		for {
			row := res.FetchRow()
			if row == nil {
				break
			}
			length, _ := row[0].(int)
			diff := length - id.origSize
			if diff < 10 && diff > -10 {
				return false
			}
		}
		return true
	},
	outputSink("%d C\n"))))))))))
}

// vim: ts=8:sw=8
