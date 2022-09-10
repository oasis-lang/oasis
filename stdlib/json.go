package stdlib

import (
	"encoding/json"
	"errors"
	"oasisgo/core"
)

type JSON struct{}

func ToJson(item any) any {
	switch item.(type) {
	case string:
		result, err := json.Marshal(item.(string))
		if err != nil {
			return err
		}
		return string(result)
	case core.OasisList:
		result := "["
		for i, v := range *item.(core.OasisList) {
			if i > 0 {
				result += ", "
			}
			result += ToJson(v).(string)
		}
		result += "]"
		return result
	case core.OasisMap:
		result, err := json.Marshal(item.(core.OasisMap))
		if err != nil {
			return err
		}
		return string(result)
	case core.Tuple:
		result := "["
		for i, v := range item.(core.Tuple).Values {
			if i > 0 {
				result += ","
			}
			result += ToJson(v).(string)
		}
		result += "]"
		return result
	case int:
		result, err := json.Marshal(item.(int))
		if err != nil {
			return err
		}
		return string(result)
	case float64:
		result, err := json.Marshal(item.(float64))
		if err != nil {
			return err
		}
		return string(result)
	case bool:
		result, err := json.Marshal(item.(bool))
		if err != nil {
			return err
		}
		return string(result)
	case uint8:
		result, err := json.Marshal(item.(uint8))
		if err != nil {
			return err
		}
		return string(result)
	case *core.Prototype:
		result, err := json.Marshal(item.(*core.Prototype).Body)
		if err != nil {
			return err
		}
		return string(result)
	}
	return errors.New("Cannot convert to JSON")
}

func (JSON) Create(vm *core.VM) (string, any) {
	return "json", &core.NativeFunction{
		Fn: func(vm *core.VM, args []any) any {
			val := ToJson(args[0])
			switch val.(type) {
			case string:
				return CreateResult(vm, val.(string), false, "")
			case error:
				return CreateResult(vm, nil, true, val.(error).Error())
			default:
				return CreateResult(vm, nil, true, "Unknown error. Please report this.")
			}
		},
		Args: 1,
	}
}
