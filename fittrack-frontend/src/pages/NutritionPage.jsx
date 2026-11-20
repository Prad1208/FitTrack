import React, { useState, useEffect } from 'react';
import api from '../api/client';
import { 
  Search, 
  Plus, 
  Trash2, 
  Utensils, 
  Check, 
  Coffee, 
  Sun, 
  Moon, 
  Cookie,
  PlusCircle
} from 'lucide-react';

export default function NutritionPage() {
  const [dailyNutrition, setDailyNutrition] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedFood, setSelectedFood] = useState(null);
  const [quantityGrams, setQuantityGrams] = useState(100);
  const [selectedMealType, setSelectedMealType] = useState('BREAKFAST');
  const [loading, setLoading] = useState(false);
  const [showCustomModal, setShowCustomModal] = useState(false);

  // Custom Food Form
  const [customFood, setCustomFood] = useState({
    name: '',
    category: 'Protein',
    caloriesPer100g: '',
    proteinPer100g: '',
    carbsPer100g: '',
    fatsPer100g: '',
    fiberPer100g: ''
  });

  useEffect(() => {
    fetchDailyNutrition();
    searchFoods('');
  }, []);

  const fetchDailyNutrition = async () => {
    try {
      const res = await api.get('/nutrition/daily');
      setDailyNutrition(res.data);
    } catch (err) {
      console.error('Failed to load daily nutrition', err);
    }
  };

  const searchFoods = async (query) => {
    try {
      const res = await api.get(`/nutrition/foods/search?query=${encodeURIComponent(query)}`);
      setSearchResults(res.data);
      if (res.data.length > 0 && !selectedFood) {
        setSelectedFood(res.data[0]);
      }
    } catch (err) {
      console.error('Failed to search foods', err);
    }
  };

  const handleSearchChange = (e) => {
    const q = e.target.value;
    setSearchQuery(q);
    searchFoods(q);
  };

  const handleAddMeal = async (e) => {
    e.preventDefault();
    if (!selectedFood || !quantityGrams) return;

    try {
      setLoading(true);
      await api.post('/nutrition/entries', {
        foodItemId: selectedFood.id,
        logDate: new Date().toISOString().split('T')[0],
        mealType: selectedMealType,
        quantityGrams: Number(quantityGrams),
      });
      await fetchDailyNutrition();
    } catch (err) {
      console.error('Failed to add meal entry', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteEntry = async (id) => {
    try {
      await api.delete(`/nutrition/entries/${id}`);
      fetchDailyNutrition();
    } catch (err) {
      console.error('Failed to delete entry', err);
    }
  };

  const handleCreateCustomFood = async (e) => {
    e.preventDefault();
    try {
      await api.post('/nutrition/foods/custom', {
        name: customFood.name,
        category: customFood.category,
        caloriesPer100g: Number(customFood.caloriesPer100g),
        proteinPer100g: Number(customFood.proteinPer100g),
        carbsPer100g: Number(customFood.carbsPer100g),
        fatsPer100g: Number(customFood.fatsPer100g),
        fiberPer100g: Number(customFood.fiberPer100g || 0),
      });
      setShowCustomModal(false);
      setCustomFood({ name: '', category: 'Protein', caloriesPer100g: '', proteinPer100g: '', carbsPer100g: '', fatsPer100g: '', fiberPer100g: '' });
      searchFoods(searchQuery);
    } catch (err) {
      console.error('Failed to create custom food', err);
    }
  };

  const factor = (quantityGrams || 0) / 100;
  const calcCals = selectedFood ? Math.round(selectedFood.caloriesPer100g * factor) : 0;
  const calcP = selectedFood ? (selectedFood.proteinPer100g * factor).toFixed(1) : 0;
  const calcC = selectedFood ? (selectedFood.carbsPer100g * factor).toFixed(1) : 0;
  const calcF = selectedFood ? (selectedFood.fatsPer100g * factor).toFixed(1) : 0;

  const mealSections = [
    { type: 'BREAKFAST', title: 'Breakfast', icon: Coffee, items: dailyNutrition?.breakfast || [] },
    { type: 'LUNCH', title: 'Lunch', icon: Sun, items: dailyNutrition?.lunch || [] },
    { type: 'DINNER', title: 'Dinner', icon: Moon, items: dailyNutrition?.dinner || [] },
    { type: 'SNACK', title: 'Snacks & Supplements', icon: Cookie, items: dailyNutrition?.snacks || [] },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">Nutrition & Food Tracker</h1>
          <p className="text-sm text-slate-400 mt-1">
            Log exact quantities in grams. Track calories and macronutrients without estimation guesswork.
          </p>
        </div>
        <button
          onClick={() => setShowCustomModal(true)}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-sm font-semibold transition-all self-start"
        >
          <PlusCircle className="w-4 h-4 text-brand-400" /> Add Custom Food
        </button>
      </div>

      {/* Main Grid: Food Search & Logger on Left, Logged Meals on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        {/* Left Column: Search & Log Form (5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Utensils className="w-4 h-4 text-brand-400" /> Log Food Item
            </h2>

            {/* Search Bar */}
            <div className="relative">
              <Search className="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400" />
              <input
                type="text"
                placeholder="Search food (roti, rajma, chole, paneer, chicken...)"
                value={searchQuery}
                onChange={handleSearchChange}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-brand-500 transition-colors"
              />
            </div>

            {/* Food Selection Chips */}
            <div className="max-h-48 overflow-y-auto space-y-1 pr-1">
              {searchResults.map((food) => (
                <button
                  key={food.id}
                  onClick={() => setSelectedFood(food)}
                  className={`w-full text-left px-3 py-2 rounded-xl text-xs font-medium flex items-center justify-between transition-colors ${
                    selectedFood?.id === food.id
                      ? 'bg-brand-500/15 text-brand-400 border border-brand-500/30'
                      : 'hover:bg-slate-800/60 text-slate-300'
                  }`}
                >
                  <span className="truncate">{food.name}</span>
                  <span className="text-[11px] text-slate-400 font-mono ml-2">
                    {Math.round(food.caloriesPer100g)} kcal / 100g
                  </span>
                </button>
              ))}
            </div>

            {/* Quantity & Meal Category Form */}
            {selectedFood && (
              <form onSubmit={handleAddMeal} className="space-y-4 pt-4 border-t border-slate-800">
                <div className="bg-slate-950/80 p-3.5 rounded-2xl border border-slate-800 space-y-2">
                  <div className="text-xs font-semibold text-slate-200">{selectedFood.name}</div>
                  <div className="grid grid-cols-4 gap-2 text-center text-xs">
                    <div className="bg-slate-900 p-2 rounded-lg">
                      <div className="text-[10px] text-slate-400">Calories</div>
                      <div className="font-mono font-bold text-amber-400">{calcCals}</div>
                    </div>
                    <div className="bg-slate-900 p-2 rounded-lg">
                      <div className="text-[10px] text-slate-400">Protein</div>
                      <div className="font-mono font-bold text-indigo-400">{calcP}g</div>
                    </div>
                    <div className="bg-slate-900 p-2 rounded-lg">
                      <div className="text-[10px] text-slate-400">Carbs</div>
                      <div className="font-mono font-bold text-cyan-400">{calcC}g</div>
                    </div>
                    <div className="bg-slate-900 p-2 rounded-lg">
                      <div className="text-[10px] text-slate-400">Fats</div>
                      <div className="font-mono font-bold text-amber-300">{calcF}g</div>
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="text-xs font-medium text-slate-400 block mb-1">Serving (Grams)</label>
                    <input
                      type="number"
                      min="1"
                      value={quantityGrams}
                      onChange={(e) => setQuantityGrams(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white font-mono focus:outline-none focus:border-brand-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-medium text-slate-400 block mb-1">Meal Category</label>
                    <select
                      value={selectedMealType}
                      onChange={(e) => setSelectedMealType(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                    >
                      <option value="BREAKFAST">Breakfast</option>
                      <option value="LUNCH">Lunch</option>
                      <option value="DINNER">Dinner</option>
                      <option value="SNACK">Snack</option>
                    </select>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-sm font-semibold shadow-lg shadow-brand-500/20 transition-all flex items-center justify-center gap-2"
                >
                  <Plus className="w-4 h-4" /> Add to {selectedMealType}
                </button>
              </form>
            )}
          </div>
        </div>

        {/* Right Column: Today's Logged Meals (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
            
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h2 className="text-base font-bold text-white">Today's Meals</h2>
              <div className="text-xs font-mono font-semibold text-slate-300">
                Total: <span className="text-amber-400 font-bold">{Math.round(dailyNutrition?.totalCalories || 0)} kcal</span> | 
                Protein: <span className="text-indigo-400 font-bold"> {Math.round(dailyNutrition?.totalProteinGrams || 0)}g</span>
              </div>
            </div>

            <div className="space-y-6">
              {mealSections.map((sec) => {
                const Icon = sec.icon;
                const secCals = sec.items.reduce((acc, curr) => acc + curr.calories, 0);

                return (
                  <div key={sec.type} className="space-y-2">
                    <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-slate-400">
                      <div className="flex items-center gap-2">
                        <Icon className="w-4 h-4 text-brand-400" />
                        <span>{sec.title}</span>
                      </div>
                      <span className="font-mono text-slate-300">{Math.round(secCals)} kcal</span>
                    </div>

                    {sec.items.length === 0 ? (
                      <div className="text-xs text-slate-500 italic py-2 pl-6">
                        No food items logged for {sec.title.toLowerCase()}
                      </div>
                    ) : (
                      <div className="space-y-1.5">
                        {sec.items.map((item) => (
                          <div
                            key={item.id}
                            className="flex items-center justify-between bg-slate-950/70 border border-slate-800/80 px-4 py-2.5 rounded-xl text-xs hover:border-slate-700 transition-colors"
                          >
                            <div>
                              <span className="font-medium text-white">{item.foodName}</span>
                              <span className="text-slate-400 ml-2 font-mono">({Math.round(item.quantityGrams)}g)</span>
                            </div>

                            <div className="flex items-center gap-4">
                              <div className="font-mono text-right">
                                <span className="text-amber-400 font-semibold">{Math.round(item.calories)} kcal</span>
                                <span className="text-slate-500 mx-1.5">•</span>
                                <span className="text-indigo-400 font-semibold">{Math.round(item.proteinGrams)}g P</span>
                              </div>
                              <button
                                onClick={() => handleDeleteEntry(item.id)}
                                title="Delete meal entry"
                                className="text-slate-500 hover:text-rose-400 transition-colors"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>

          </div>
        </div>

      </div>

      {/* Modal: Custom Food Creator */}
      {showCustomModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white">Create Custom Food Item</h3>
            <p className="text-xs text-slate-400">Provide nutritional values strictly per 100 grams.</p>

            <form onSubmit={handleCreateCustomFood} className="space-y-3">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Food Name</label>
                <input
                  type="text"
                  required
                  value={customFood.name}
                  onChange={(e) => setCustomFood({ ...customFood, name: e.target.value })}
                  placeholder="e.g. Grandma's Protein Bar"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs text-slate-400 block mb-1">Calories (per 100g)</label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    value={customFood.caloriesPer100g}
                    onChange={(e) => setCustomFood({ ...customFood, caloriesPer100g: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-400 block mb-1">Protein (g)</label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    value={customFood.proteinPer100g}
                    onChange={(e) => setCustomFood({ ...customFood, proteinPer100g: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-400 block mb-1">Carbs (g)</label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    value={customFood.carbsPer100g}
                    onChange={(e) => setCustomFood({ ...customFood, carbsPer100g: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-400 block mb-1">Fats (g)</label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    value={customFood.fatsPer100g}
                    onChange={(e) => setCustomFood({ ...customFood, fatsPer100g: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white font-mono"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowCustomModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold shadow-md"
                >
                  Save Food
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
