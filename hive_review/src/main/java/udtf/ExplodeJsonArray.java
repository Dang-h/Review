package udtf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ExplodeJsonArray extends GenericUDTF {


	/**
	 * 在process之前调用，调用一次，process每一行调用一次；<p>
	 * 对输入函数的数据进行个数和类型的校验，将输出的数据进行封装
	 *
	 * @param argOIs 函数输入的数据
	 * @return 函数处理完的结果
	 * @throws UDFArgumentException 函数参数异常
	 */
	@Override
	public StructObjectInspector initialize(StructObjectInspector argOIs) throws UDFArgumentException {

		// 获取传入数据结构的引用
		List<? extends StructField> allStructFieldRefs = argOIs.getAllStructFieldRefs();

		// 获取传入参数的类型
		String typeName = allStructFieldRefs.get(0).getFieldObjectInspector().getTypeName();

		// 判断传入参数的个数和类型
		if (!"string".equals(typeName) && allStructFieldRefs.size() != 1) {
			throw new UDFArgumentException("参数个数异常，只能传入一个类型为String的参数");
		}

		// 字段名
		ArrayList<String> fieldNames = new ArrayList<>();
		// 字段类型检查器
		ArrayList<ObjectInspector> fieldOIs = new ArrayList<>();

		// 不指定列名时的默认列名
		fieldNames.add("items");
		// 传入函数的类型为基本的数据类型--String
		fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

		// 处理完的数据需要返回复杂的结构体数据类型
		return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
	}

	/**
	 * 对输入的数据进行处理,每一行调用一次
	 *
	 * @param objects 需处理的数据
	 * @throws HiveException
	 */
	@Override
	public void process(Object[] objects) throws HiveException {
		// 将传入的数据转成String
		String datas = objects[0].toString();
		//遍历json数组
		JSONArray jsonArray = new JSONArray(datas);
//		StringBuilder stringBuilder = new StringBuilder(1);
		String[] stringBuilder = new String[1];
		for (int i = 0; i < jsonArray.length(); i++) {
			stringBuilder[0] = jsonArray.getString(i);

			// 使用forward输出数据
			forward(stringBuilder);
		}
	}

	@Override
	public void close() throws HiveException {

	}

}
